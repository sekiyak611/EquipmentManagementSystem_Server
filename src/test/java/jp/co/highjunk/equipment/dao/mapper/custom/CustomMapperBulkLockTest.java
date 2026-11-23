package jp.co.highjunk.equipment.dao.mapper.custom;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * B-04 で足した、まとめて押さえるメソッド（LoanCustomMapper#selectCurrentByEquipmentIdsForUpdate・EquipmentCustomMapper#selectByIdsForUpdate）が、
 * 本当に行を押さえていることのテスト（詳細設計 equipment/mine/B-04_確認した.md の8章）。
 * 押さえている間に、別の接続が同じ行を NOWAIT で押さえようとすると、「押さえられない」誤り（MySQL の 3572）になることで確かめる（待たない）。
 * 別のまとまりを確定させるので、@Transactional を付けず、最後に入れた行を消して元に戻す。
 */
@SpringBootTest
@DisplayName("まとめて押さえるメソッドが、行を押さえていること")
class CustomMapperBulkLockTest {

	static final int ER_LOCK_NOWAIT = 3572;

	/** MySQL のエラー番号：行を待ちきれなかった（ER_LOCK_WAIT_TIMEOUT） */
	static final int ER_LOCK_WAIT_TIMEOUT = 1205;

	@Autowired
	LoanCustomMapper loanCustomMapper;

	@Autowired
	EquipmentCustomMapper equipmentCustomMapper;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	TransactionTemplate transaction;

	@Autowired
	Clock clock;

	@AfterEach
	void cleanUp() {
		jdbc.update("DELETE FROM loans WHERE equipment_id IN (SELECT id FROM equipment WHERE management_no LIKE 'BLOCK%')");
		jdbc.update("DELETE FROM equipment WHERE management_no LIKE 'BLOCK%'");
	}

	@Test
	@DisplayName("貸出履歴：押さえている間、別の接続は、その貸出履歴の行を押さえられない。押さえていない貸出履歴（送っていない備品）は押さえられる")
	void loansAreLocked() throws Exception {
		long e1 = equipment("BLOCK1");
		long e2 = equipment("BLOCK2");
		long l1 = loan(e1);
		long l2 = loan(e2);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		try {
			Future<?> holder = executor.submit(() -> transaction.executeWithoutResult(status -> {
				loanCustomMapper.selectCurrentByEquipmentIdsForUpdate(List.of(e1));
				locked.countDown();
				await(release);
			}));
			assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

			assertThat(canLock("loans", l1)).isFalse();
			assertThat(canLock("loans", l2)).isTrue();

			release.countDown();
			holder.get(10, TimeUnit.SECONDS);
			assertThat(canLock("loans", l1)).isTrue();
		} finally {
			release.countDown();
			executor.shutdownNow();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}

	@Test
	@DisplayName("備品：押さえている間、別の接続は、その備品の行を押さえられない。送っていない備品は押さえられる")
	void equipmentIsLocked() throws Exception {
		long e1 = equipment("BLOCK3");
		long e2 = equipment("BLOCK4");
		ExecutorService executor = Executors.newSingleThreadExecutor();
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		try {
			Future<?> holder = executor.submit(() -> transaction.executeWithoutResult(status -> {
				equipmentCustomMapper.selectByIdsForUpdate(List.of(e1));
				locked.countDown();
				await(release);
			}));
			assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

			assertThat(canLock("equipment", e1)).isFalse();
			assertThat(canLock("equipment", e2)).isTrue();

			release.countDown();
			holder.get(10, TimeUnit.SECONDS);
			assertThat(canLock("equipment", e1)).isTrue();
		} finally {
			release.countDown();
			executor.shutdownNow();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}

	@Test
	@DisplayName("貸出履歴：借りられていない備品の ID を送ると、その備品の「今借りている貸出履歴」の隙間を押さえる。別の接続が、その備品の貸出履歴を足そうとすると、待たされる（B-02 との行き違い防止）。放したあとは、足せる")
	void gapOfFreeEquipmentIsLocked() throws Exception {
		long free = equipment("BLOCK5");
		ExecutorService executor = Executors.newSingleThreadExecutor();
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		try {
			Future<?> holder = executor.submit(() -> transaction.executeWithoutResult(status -> {
				assertThat(loanCustomMapper.selectCurrentByEquipmentIdsForUpdate(List.of(free))).isEmpty();
				locked.countDown();
				await(release);
			}));
			assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

			// 隙間ロックは、前後の今借りている行の間の隙間ぜんぶ（その中に入るほかの備品の貸出履歴も、短く待たされる。B-04 の8章）
			assertThat(insertLoanWaits(free)).as("送った備品の貸出履歴を足す").isTrue();

			release.countDown();
			holder.get(10, TimeUnit.SECONDS);
			assertThat(insertLoanWaits(free)).as("放したあとは足せる").isFalse();
		} finally {
			release.countDown();
			executor.shutdownNow();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}

	@Test
	@DisplayName("備品を押さえる順が、備品の ID の昇順なので、逆の順で送る2つのまとまりを、何度同時に動かしても、デッドロックしない")
	void reversedOrderDoesNotDeadlock() throws Exception {
		long e1 = equipment("BLOCK7");
		long e2 = equipment("BLOCK8");
		loan(e1);
		loan(e2);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			for (int round = 0; round < 30; round++) {
				CountDownLatch start = new CountDownLatch(1);
				Future<?> a = executor.submit(() -> lockBoth(start, List.of(e1, e2)));
				Future<?> b = executor.submit(() -> lockBoth(start, List.of(e2, e1)));
				start.countDown();
				a.get(20, TimeUnit.SECONDS);
				b.get(20, TimeUnit.SECONDS);
			}
		} finally {
			executor.shutdownNow();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}

	/** 貸出履歴 → 備品の順に、押さえる（B-04 の ServiceImpl と同じ）。送る順は、わざと変える。 */
	private void lockBoth(CountDownLatch start, List<Long> ids) {
		await(start);
		transaction.executeWithoutResult(status -> {
			loanCustomMapper.selectCurrentByEquipmentIdsForUpdate(ids);
			equipmentCustomMapper.selectByIdsForUpdate(ids);
			try {
				Thread.sleep(20); // 押さえたまま、少し持つ（行き違いを起こしやすくする）
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
	}

	/** 別の接続で、その備品の貸出履歴を足してみる。待たされて誤り（1205）になれば true。足せれば（そのあと消す）false。 */
	private boolean insertLoanWaits(long equipmentId) {
		LocalDateTime now = LocalDateTime.now(clock);
		long userId = jdbc.queryForObject("SELECT id FROM users WHERE email = 'ippan@example.com'", Long.class);
		try {
			transaction.executeWithoutResult(status -> {
				jdbc.execute("SET SESSION innodb_lock_wait_timeout = 1");
				try {
					jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, created_at, updated_at)"
							+ " VALUES (?, ?, ?, ?, ?)", equipmentId, userId, LocalDate.now(clock), now, now);
				} finally {
					jdbc.execute("SET SESSION innodb_lock_wait_timeout = DEFAULT");
				}
			});
			jdbc.update("DELETE FROM loans WHERE equipment_id = ?", equipmentId);
			return false;
		} catch (RuntimeException e) {
			Throwable root = NestedExceptionUtils.getRootCause(e);
			if (root instanceof SQLException sql && sql.getErrorCode() == ER_LOCK_WAIT_TIMEOUT) {
				return true;
			}
			throw e;
		}
	}

	/** 別の接続で、その行を NOWAIT で押さえてみる。押さえられれば true、押さえられない（3572）なら false。 */
	private boolean canLock(String table, long id) {
		try {
			transaction.executeWithoutResult(status -> jdbc
					.queryForObject("SELECT id FROM " + table + " WHERE id = ? FOR UPDATE NOWAIT", Long.class, id));
			return true;
		} catch (RuntimeException e) {
			Throwable root = NestedExceptionUtils.getRootCause(e);
			if (root instanceof SQLException sql && sql.getErrorCode() == ER_LOCK_NOWAIT) {
				return false;
			}
			throw e;
		}
	}

	long equipment(String managementNo) {
		LocalDateTime now = LocalDateTime.now(clock);
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, created_at, updated_at)"
				+ " VALUES (?, 1, 'MANAGED', ?, ?)", managementNo, now, now);
		return jdbc.queryForObject("SELECT id FROM equipment WHERE management_no = ?", Long.class, managementNo);
	}

	long loan(long equipmentId) {
		LocalDateTime now = LocalDateTime.now(clock);
		long userId = jdbc.queryForObject("SELECT id FROM users WHERE email = 'ippan@example.com'", Long.class);
		jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
				equipmentId, userId, LocalDate.now(clock), now, now);
		return jdbc.queryForObject("SELECT id FROM loans WHERE equipment_id = ?", Long.class, equipmentId);
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
