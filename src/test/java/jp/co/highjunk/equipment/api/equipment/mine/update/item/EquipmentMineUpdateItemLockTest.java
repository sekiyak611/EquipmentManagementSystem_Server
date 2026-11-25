package jp.co.highjunk.equipment.api.equipment.mine.update.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;

import jp.co.highjunk.equipment.api.equipment.mine.update.item.service.EquipmentMineUpdateItemService;
import jp.co.highjunk.equipment.api.equipment.mine.update.item.service.EquipmentMineUpdateItemServiceInDto;
import jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.service.EquipmentMineUpdateReturnItemService;
import jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.service.EquipmentMineUpdateReturnItemServiceInDto;
import jp.co.highjunk.equipment.common.error.ForbiddenException;
import jp.co.highjunk.equipment.testsupport.EquipmentMineLockTestBase;

/**
 * B-04 の、同時に操作されたときの守り（詳細設計 equipment/mine/B-04_確認した.md の8章）のテスト。
 * B-04 は、備品の行だけを、備品の ID の昇順に押さえる（貸出履歴は押さえない。障害 ZPOP-434）。B-03（返す）は、貸出履歴 → 備品の順に押さえる。
 * 「待たされている」ことは、B-04（または B-03）が終わらないこと（1秒）と、そのあとの結果で確かめる。遅い機械でも、動き出す前に
 * 確かめてしまわないよう、先に別の貸出履歴で B-04 を1回動かして、温めておく。
 * 2つのまとまりを別々に確定させるので、@Transactional を付けない（EquipmentMineLockTestBase）。
 */
@DisplayName("B-04 自分が借りている備品を「確認した」にする：同時に操作されたとき")
class EquipmentMineUpdateItemLockTest extends EquipmentMineLockTestBase {

	/** MySQL のエラー番号：NOWAIT で行を押さえられない（ER_LOCK_NOWAIT） */
	static final int ER_LOCK_NOWAIT = 3572;

	@Autowired
	EquipmentMineUpdateItemService confirmService;

	@Autowired
	EquipmentMineUpdateReturnItemService returnService;

	@Override
	protected String prefix() {
		return "CLOCK";
	}

	// ---------- B-03（返す）との組み合わせ（8章の場合分け②③④） ----------

	@Test
	@DisplayName("場合分け②：返す人（B-03 や総務の C-08）が、備品の行を押さえて、返却を確定する前：B-04 は、備品の行で待たされ、返却が確定したあとで確かめて 403（借りていない）。確認は、されない")
	void waitsForReturnHoldingEquipment() throws Exception {
		long userId = ippanId();
		warmUp(userId);
		long loanId = newLoan(prefix() + "1");
		long equipmentId = equipmentIdOf(loanId);
		Map<String, Object> before = equipmentRow(equipmentId);
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch commit = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			// 返す側：貸出履歴 → 備品の順に行を押さえ（B-03 と同じ）、返却日と最終確認日・version を入れて、確定させずに待つ
			Future<?> returning = executor.submit(() -> transaction.executeWithoutResult(status -> {
				jdbc.queryForObject("SELECT id FROM loans WHERE id = ? FOR UPDATE", Long.class, loanId);
				jdbc.queryForObject("SELECT id FROM equipment WHERE id = ? FOR UPDATE", Long.class, equipmentId);
				jdbc.update("UPDATE loans SET returned_on = ?, version = version + 1 WHERE id = ?", LocalDate.now(clock),
						loanId);
				jdbc.update("UPDATE equipment SET last_confirmed_on = ?, version = version + 1 WHERE id = ?",
						LocalDate.now(clock), equipmentId);
				locked.countDown();
				await(commit);
			}));
			assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

			// B-04：備品の行を押さえようとして、待たされる
			Future<?> confirming = executor.submit(
					() -> confirmService.confirm(new EquipmentMineUpdateItemServiceInDto(List.of(equipmentId), userId)));
			assertThatThrownBy(() -> confirming.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);

			// 返す人が確定させると、B-04 が動き、最新の記録で、もう借りていないと分かって 403 になる
			commit.countDown();
			returning.get(10, TimeUnit.SECONDS);
			assertThatThrownBy(() -> confirming.get(10, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
					.cause().isInstanceOf(ForbiddenException.class)
					.hasMessage("借りていない備品が含まれているため、確認できません。");
		} finally {
			commit.countDown();
			stop(executor);
		}
		// 備品は、返す側の書き換えのまま（version は、返却の1つだけ増えた。B-04 は何も書いていない）
		assertThat(equipmentRow(equipmentId).get("version")).isEqualTo(intOf(before, "version") + 1);
		assertThat(loanRow(loanId).get("version")).isEqualTo(1);
	}

	@Test
	@DisplayName("場合分け③：返す人が、貸出履歴の行だけを押さえていて、備品の行はまだ：B-04 は、待たずに進み、成功する。そのあとで、返す人が進み、返す（確認が先、そのあとで返却。version は2つ増える）")
	void doesNotWaitForLoanRowOnly() throws Exception {
		long userId = ippanId();
		warmUp(userId);
		long loanId = newLoan(prefix() + "4");
		long equipmentId = equipmentIdOf(loanId);
		CountDownLatch loanLocked = new CountDownLatch(1);
		CountDownLatch proceed = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			// 返す側（B-03 と同じ手順）：貸出履歴の行を押さえて、止まる。合図が来たら、備品の行を押さえ、返却を書き、確定する
			Future<?> returning = executor.submit(() -> transaction.executeWithoutResult(status -> {
				jdbc.queryForObject("SELECT id FROM loans WHERE id = ? FOR UPDATE", Long.class, loanId);
				loanLocked.countDown();
				await(proceed);
				jdbc.queryForObject("SELECT id FROM equipment WHERE id = ? FOR UPDATE", Long.class, equipmentId);
				jdbc.update("UPDATE loans SET returned_on = ?, version = version + 1 WHERE id = ?", LocalDate.now(clock),
						loanId);
				jdbc.update("UPDATE equipment SET last_confirmed_on = ?, version = version + 1 WHERE id = ?",
						LocalDate.now(clock), equipmentId);
			}));
			assertThat(loanLocked.await(10, TimeUnit.SECONDS)).isTrue();

			// B-04 は、貸出履歴の行を押さえないので、待たされない（10秒以内に、成功する）
			Future<?> confirming = executor.submit(
					() -> confirmService.confirm(new EquipmentMineUpdateItemServiceInDto(List.of(equipmentId), userId)));
			confirming.get(10, TimeUnit.SECONDS);
			assertThat(equipmentRow(equipmentId).get("last_confirmed_on")).hasToString(LocalDate.now(clock).toString());
			assertThat(equipmentRow(equipmentId).get("version")).isEqualTo(1);

			// そのあとで、返す人が進み、返す
			proceed.countDown();
			returning.get(10, TimeUnit.SECONDS);
		} finally {
			proceed.countDown();
			stop(executor);
		}
		assertThat(loanRow(loanId).get("returned_on")).hasToString(LocalDate.now(clock).toString());
		assertThat(loanRow(loanId).get("version")).isEqualTo(1);
		assertThat(equipmentRow(equipmentId).get("version")).isEqualTo(2);
	}

	@Test
	@DisplayName("場合分け④：B-04 が先に備品の行を押さえて、更新している間：返す人（B-03）は、備品の行で待たされ、B-04 が終わってから進んで、返す（確認が先、そのあとで返却。version は2つ増える）")
	void returnWaitsForConfirm() throws Exception {
		long userId = ippanId();
		warmUp(userId);
		long loanId = newLoan(prefix() + "5");
		long equipmentId = equipmentIdOf(loanId);
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			// B-04 が備品の行を押さえて、最終確認日を書いて、確定する前の状態
			Future<?> confirming = executor.submit(() -> transaction.executeWithoutResult(status -> {
				jdbc.queryForObject("SELECT id FROM equipment WHERE id = ? FOR UPDATE", Long.class, equipmentId);
				jdbc.update("UPDATE equipment SET last_confirmed_on = ?, version = version + 1 WHERE id = ?",
						LocalDate.now(clock), equipmentId);
				locked.countDown();
				await(release);
			}));
			assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

			Future<?> returning = executor.submit(
					() -> returnService.returnItem(new EquipmentMineUpdateReturnItemServiceInDto(loanId, null, userId)));
			assertThatThrownBy(() -> returning.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);

			release.countDown();
			confirming.get(10, TimeUnit.SECONDS);
			returning.get(10, TimeUnit.SECONDS);
		} finally {
			release.countDown();
			stop(executor);
		}
		assertThat(loanRow(loanId).get("returned_on")).hasToString(LocalDate.now(clock).toString());
		assertThat(loanRow(loanId).get("version")).isEqualTo(1);
		assertThat(equipmentRow(equipmentId).get("version")).isEqualTo(2);
	}

	@Test
	@DisplayName("B-03（返す）と B-04（確認する）を、同じ備品に、何度同時に動かしても、デッドロックしない。どちらが先でも、つじつまが合う（返しが先なら B-04 は 403、確認が先なら両方成功）")
	void returnAndConfirmDoNotDeadlock() throws Exception {
		long userId = ippanId();
		warmUp(userId);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			for (int round = 0; round < 15; round++) {
				long loanId = newLoan(prefix() + "R" + round);
				long equipmentId = equipmentIdOf(loanId);
				CountDownLatch start = new CountDownLatch(1);
				Future<?> returning = executor.submit(() -> {
					await(start);
					returnService.returnItem(new EquipmentMineUpdateReturnItemServiceInDto(loanId, null, userId));
				});
				Future<String> confirming = executor.submit(() -> {
					await(start);
					try {
						confirmService.confirm(new EquipmentMineUpdateItemServiceInDto(List.of(equipmentId), userId));
						return "成功";
					} catch (ForbiddenException e) {
						return e.getMessage();
					}
				});
				start.countDown();
				returning.get(20, TimeUnit.SECONDS);
				String confirmResult = confirming.get(20, TimeUnit.SECONDS);

				// 返しは、いつも成功する。B-04 は、成功か、403（借りていない備品が含まれているため）のどちらかだけ
				assertThat(confirmResult).isIn("成功", "借りていない備品が含まれているため、確認できません。");
				assertThat(loanRow(loanId).get("returned_on")).hasToString(LocalDate.now(clock).toString());
				assertThat(loanRow(loanId).get("version")).isEqualTo(1); // B-04 は、貸出履歴を変えない
				Map<String, Object> eq = equipmentRow(equipmentId);
				assertThat(eq.get("last_confirmed_on")).hasToString(LocalDate.now(clock).toString());
				// 確認が先に成功したなら、version は2つ（確認と返却）。返しが先なら、1つだけ（返却）
				assertThat(eq.get("version")).isEqualTo("成功".equals(confirmResult) ? 2 : 1);
			}
		} finally {
			stop(executor);
		}
	}

	// ---------- B-04 どうし（8章の「押さえる行」） ----------

	@Test
	@DisplayName("備品を、送られた順ではなく、ID の昇順に押さえる：小さい ID の備品を別の人が押さえている間、[大, 小] の順で送った B-04 は、大きい ID の備品を、まだ押さえていない（押さえていれば、行き違いになる）")
	void locksInAscendingOrder() throws Exception {
		long userId = ippanId();
		warmUp(userId);
		long loanSmall = newLoan(prefix() + "6");
		long loanLarge = newLoan(prefix() + "7");
		long small = equipmentIdOf(loanSmall);
		long large = equipmentIdOf(loanLarge);
		assertThat(small).isLessThan(large);
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<?> holder = executor.submit(() -> transaction.executeWithoutResult(status -> {
				jdbc.queryForObject("SELECT id FROM equipment WHERE id = ? FOR UPDATE", Long.class, small);
				locked.countDown();
				await(release);
			}));
			assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

			// 送る順は、[大, 小]。昇順に押さえるなら、小さい備品（保持役が押さえている）で待たされ、大きい備品は、まだ押さえていない
			Future<?> confirming = executor.submit(
					() -> confirmService.confirm(new EquipmentMineUpdateItemServiceInDto(List.of(large, small), userId)));
			assertThatThrownBy(() -> confirming.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);
			assertThat(canLockEquipmentNowait(large)).as("大きい備品は、まだ押さえられていない").isTrue();

			release.countDown();
			holder.get(10, TimeUnit.SECONDS);
			confirming.get(10, TimeUnit.SECONDS);
		} finally {
			release.countDown();
			stop(executor);
		}
		assertThat(equipmentRow(small).get("version")).isEqualTo(1);
		assertThat(equipmentRow(large).get("version")).isEqualTo(1);
	}

	@Test
	@DisplayName("逆の順で送る2つの B-04 を、何度同時に動かしても、デッドロックせず、両方成功する。version は、成功した回数だけ増える")
	void reversedOrderDoesNotDeadlock() throws Exception {
		long userId = ippanId();
		long l1 = newLoan(prefix() + "2");
		long l2 = newLoan(prefix() + "3");
		long e1 = equipmentIdOf(l1);
		long e2 = equipmentIdOf(l2);
		int rounds = 20;
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			for (int round = 0; round < rounds; round++) {
				CountDownLatch start = new CountDownLatch(1);
				Future<?> a = executor.submit(() -> confirmAfter(start, List.of(e1, e2), userId));
				Future<?> b = executor.submit(() -> confirmAfter(start, List.of(e2, e1), userId));
				start.countDown();
				a.get(20, TimeUnit.SECONDS);
				b.get(20, TimeUnit.SECONDS);
			}
		} finally {
			stop(executor);
		}
		assertThat(equipmentRow(e1).get("version")).isEqualTo(rounds * 2);
		assertThat(equipmentRow(e2).get("version")).isEqualTo(rounds * 2);
	}

	// ---------- 道具 ----------

	private void confirmAfter(CountDownLatch start, List<Long> equipmentIds, long userId) {
		await(start);
		confirmService.confirm(new EquipmentMineUpdateItemServiceInDto(equipmentIds, userId));
	}

	/** 別の貸出履歴で B-04 を1回動かし、最初の呼び出しの遅さが、待ちの確かめに混ざらないようにする。 */
	private void warmUp(long userId) {
		long loanId = newLoan(prefix() + "0");
		confirmService.confirm(new EquipmentMineUpdateItemServiceInDto(List.of(equipmentIdOf(loanId)), userId));
	}

	/** 別の接続で、その備品の行を NOWAIT で押さえてみる。押さえられれば true、押さえられない（3572）なら false。 */
	private boolean canLockEquipmentNowait(long equipmentId) {
		try {
			transaction.executeWithoutResult(status -> jdbc
					.queryForObject("SELECT id FROM equipment WHERE id = ? FOR UPDATE NOWAIT", Long.class, equipmentId));
			return true;
		} catch (RuntimeException e) {
			Throwable root = NestedExceptionUtils.getRootCause(e);
			if (root instanceof SQLException sql && sql.getErrorCode() == ER_LOCK_NOWAIT) {
				return false;
			}
			throw e;
		}
	}

	Map<String, Object> equipmentRow(long equipmentId) {
		return jdbc.queryForMap("SELECT last_confirmed_on, version, updated_by, updated_at, status FROM equipment WHERE id = ?",
				equipmentId);
	}

	Map<String, Object> loanRow(long loanId) {
		return jdbc.queryForMap("SELECT returned_on, version FROM loans WHERE id = ?", loanId);
	}

	static int intOf(Map<String, Object> row, String key) {
		return ((Number) row.get(key)).intValue();
	}
}
