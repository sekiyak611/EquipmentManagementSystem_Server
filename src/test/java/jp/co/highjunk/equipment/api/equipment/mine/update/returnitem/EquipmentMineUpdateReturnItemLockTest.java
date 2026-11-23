package jp.co.highjunk.equipment.api.equipment.mine.update.returnitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.service.EquipmentMineUpdateReturnItemService;
import jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.service.EquipmentMineUpdateReturnItemServiceInDto;
import jp.co.highjunk.equipment.common.error.ConflictException;

/**
 * B-03 の、同時に操作されたときの守り（詳細設計 equipment/mine/B-03_備品を返す.md の7章 No.3・8章）のテスト。
 * 2つのまとまり（トランザクション）を別々に確定させるので、このテストは @Transactional を付けず、最後に入れた行を消して元に戻す。
 * 「待たされている」ことは、B-03 が終わらないこと（1秒）と、そのあとの結果で確かめる。
 * 遅い機械でも、B-03 が動き出す前に確かめてしまわないよう、先に別の貸出履歴で B-03 を1回動かして、温めておく。
 */
@SpringBootTest
@DisplayName("B-03 備品を返す：同時に操作されたとき")
class EquipmentMineUpdateReturnItemLockTest {

	static final String PREFIX = "RLOCK";

	/** MySQL のエラー番号：NOWAIT で行を押さえられない（ER_LOCK_NOWAIT） */
	static final int ER_LOCK_NOWAIT = 3572;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	TransactionTemplate transaction;

	@Autowired
	EquipmentMineUpdateReturnItemService service;

	@Autowired
	Clock clock;

	@AfterEach
	void cleanUp() {
		jdbc.update("DELETE FROM loans WHERE equipment_id IN (SELECT id FROM equipment WHERE management_no LIKE ?)",
				PREFIX + "%");
		jdbc.update("DELETE FROM equipment WHERE management_no LIKE ?", PREFIX + "%");
	}

	@Test
	@DisplayName("先の人が貸出履歴の行を押さえている間、B-03 は待たされ、先の人が返したあとで確かめて 409（二重に返さない）。先に普通の SELECT で古い記録を読んでいても、二重に返さない")
	void waitsForOtherReturn() throws Exception {
		long userId = userId();
		warmUp(userId);
		long loanId = newLoan(PREFIX + "1");
		Map<String, Object> afterFirstReturn;
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch commit = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			// 先に返す側（たとえば、総務が C-08 で返す）：貸出履歴の行を FOR UPDATE で押さえ、返却日を入れて、確定させずに待つ
			Future<?> first = executor.submit(() -> transaction.executeWithoutResult(status -> {
				jdbc.queryForObject("SELECT id FROM loans WHERE id = ? FOR UPDATE", Long.class, loanId);
				jdbc.update("UPDATE loans SET returned_on = ?, version = version + 1 WHERE id = ?",
						LocalDate.now(clock), loanId);
				locked.countDown();
				await(commit);
			}));
			assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

			// B-03：同じまとまりの中で、先に貸出履歴を普通の SELECT で読む（このとき、返却日はまだ空。MySQL は、この時点の記録を、
			// このまとまりの間、普通の SELECT に見せ続ける）。そのあとで B-03 を呼ぶと、貸出履歴の行を押さえようとして、待たされる
			Future<?> second = executor.submit(() -> transaction.execute(status -> {
				assertThat(jdbc.queryForObject("SELECT returned_on FROM loans WHERE id = ?", Object.class, loanId))
						.isNull();
				return service.returnItem(new EquipmentMineUpdateReturnItemServiceInDto(loanId, "あとから", userId));
			}));
			assertThatThrownBy(() -> second.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);

			// 先の人が確定させると、B-03 が動き、最新の記録で、返却済みと分かって 409 になる（古い記録を信じると、二重に返してしまう）
			commit.countDown();
			first.get(10, TimeUnit.SECONDS);
			assertThatThrownBy(() -> second.get(10, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
					.cause().isInstanceOf(ConflictException.class).hasMessage("この備品はすでに返却されています");
			afterFirstReturn = jdbc.queryForMap("SELECT l.version, l.memo, e.version AS e_version,"
					+ " e.last_confirmed_on FROM loans l JOIN equipment e ON e.id = l.equipment_id WHERE l.id = ?",
					loanId);
		} finally {
			commit.countDown();
			stop(executor);
		}
		// 先の人の書き換えのまま。B-03 は、貸出履歴にも備品にも、何も書いていない
		assertThat(afterFirstReturn.get("version")).isEqualTo(1);
		assertThat(afterFirstReturn.get("memo")).isNull();
		assertThat(afterFirstReturn.get("e_version")).isEqualTo(0);
		assertThat(afterFirstReturn.get("last_confirmed_on")).isNull();
	}

	@Test
	@DisplayName("備品の行を別の人が押さえていても、B-03 は先に貸出履歴の行を押さえている（貸出履歴 → 備品の順）。備品が空いたら返せる")
	void lockOrderLoanThenEquipment() throws Exception {
		long userId = userId();
		warmUp(userId);
		long loanId = newLoan(PREFIX + "2");
		long equipmentId = jdbc.queryForObject("SELECT equipment_id FROM loans WHERE id = ?", Long.class, loanId);
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			// 別の人：備品の行だけを押さえて待つ
			Future<?> holder = executor.submit(() -> transaction.executeWithoutResult(status -> {
				jdbc.queryForObject("SELECT id FROM equipment WHERE id = ? FOR UPDATE", Long.class, equipmentId);
				locked.countDown();
				await(release);
			}));
			assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

			// B-03：貸出履歴の行を押さえ、備品の行で待たされる
			Future<?> returning = executor.submit(
					() -> service.returnItem(new EquipmentMineUpdateReturnItemServiceInDto(loanId, null, userId)));
			assertThatThrownBy(() -> returning.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);

			// B-03 が貸出履歴の行を押さえるのは、備品の行で待たされるより前。押さえるまで、待たずに調べ直す（最大10秒）。
			// 押さえられたら、「ほかの人が押さえている」という MySQL の誤り（3572）になる
			assertThat(awaitLoanRowLocked(loanId)).isTrue();

			// 備品が空くと、B-03 が終わり、返却される。貸出履歴・備品の書き換えが、そろっている
			release.countDown();
			holder.get(10, TimeUnit.SECONDS);
			returning.get(10, TimeUnit.SECONDS);
			Map<String, Object> row = jdbc.queryForMap("SELECT l.returned_on, l.version, l.memo, e.version AS e_version,"
					+ " e.last_confirmed_on FROM loans l JOIN equipment e ON e.id = l.equipment_id WHERE l.id = ?",
					loanId);
			assertThat(row.get("returned_on")).hasToString(LocalDate.now(clock).toString());
			assertThat(row.get("version")).isEqualTo(1);
			assertThat(row.get("memo")).isNull();
			assertThat(row.get("e_version")).isEqualTo(1);
			assertThat(row.get("last_confirmed_on")).hasToString(LocalDate.now(clock).toString());
		} finally {
			release.countDown();
			stop(executor);
		}
	}

	/** 貸出履歴の行を押さえようとして、押さえられない（3572）ことを、押さえられなくなるまで、0.1秒おきに最大10秒、試す。 */
	private boolean awaitLoanRowLocked(long loanId) throws InterruptedException {
		long limit = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (System.nanoTime() < limit) {
			try {
				transaction.executeWithoutResult(status -> jdbc
						.queryForObject("SELECT id FROM loans WHERE id = ? FOR UPDATE NOWAIT", Long.class, loanId));
			} catch (RuntimeException e) {
				Throwable root = NestedExceptionUtils.getRootCause(e);
				if (root instanceof SQLException sql && sql.getErrorCode() == ER_LOCK_NOWAIT) {
					return true;
				}
				throw e;
			}
			Thread.sleep(100);
		}
		return false;
	}

	/** 別の貸出履歴で B-03 を1回動かし、最初の呼び出しの遅さ（クラスの読み込みなど）が、待ちの確かめに混ざらないようにする。 */
	private void warmUp(long userId) {
		long loanId = newLoan(PREFIX + "0");
		service.returnItem(new EquipmentMineUpdateReturnItemServiceInDto(loanId, null, userId));
	}

	long userId() {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = 'ippan@example.com'", Long.class);
	}

	/** 一般社員が借りている、管理中の備品の貸出履歴を入れ、貸出履歴の ID を返す（管理番号で、入れた行を引く） */
	long newLoan(String managementNo) {
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate today = now.toLocalDate();
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, created_at, updated_at)"
				+ " VALUES (?, 1, 'MANAGED', ?, ?)", managementNo, now, now);
		long equipmentId = jdbc.queryForObject("SELECT id FROM equipment WHERE management_no = ?", Long.class,
				managementNo);
		jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
				equipmentId, userId(), today.minusDays(2), now, now);
		return jdbc.queryForObject("SELECT id FROM loans WHERE equipment_id = ?", Long.class, equipmentId);
	}

	/** 動いているまとまりを止め、終わるのを待つ（残ったまとまりが、後始末の削除や、次のテストの邪魔をしないため） */
	private static void stop(ExecutorService executor) throws InterruptedException {
		executor.shutdownNow();
		executor.awaitTermination(10, TimeUnit.SECONDS);
	}

	private static void await(CountDownLatch latch) {
		try {
			if (!latch.await(30, TimeUnit.SECONDS)) {
				throw new IllegalStateException("合図が来なかった");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("待っている途中で止められた", e);
		}
	}
}
