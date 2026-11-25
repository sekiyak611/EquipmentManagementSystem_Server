package jp.co.highjunk.equipment.dao.mapper.custom;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.time.Clock;
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
 * EquipmentCustomMapper#selectByIdsForUpdate（B-04 で足した、備品をまとめて押さえるメソッド）が、本当に行を押さえていることのテスト
 * （詳細設計 equipment/mine/B-04_確認した.md の8章）。
 * 押さえている間に、別の接続が同じ行を NOWAIT で押さえようとすると、「押さえられない」誤り（MySQL の 3572）になることで確かめる（待たない）。
 * 別のまとまりを確定させるので、@Transactional を付けず、最後に入れた行を消して元に戻す。
 */
@SpringBootTest
@DisplayName("EquipmentCustomMapper#selectByIdsForUpdate が、行を押さえていること")
class EquipmentCustomMapperLockTest {

	static final int ER_LOCK_NOWAIT = 3572;

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
		jdbc.update("DELETE FROM equipment WHERE management_no LIKE 'BLOCK%'");
	}

	@Test
	@DisplayName("押さえている間、別の接続は、その備品の行を押さえられない。送っていない備品は押さえられる。放したあとは押さえられる")
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

			assertThat(canLock(e1)).isFalse();
			assertThat(canLock(e2)).isTrue();

			release.countDown();
			holder.get(10, TimeUnit.SECONDS);
			assertThat(canLock(e1)).isTrue();
		} finally {
			release.countDown();
			executor.shutdownNow();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}

	@Test
	@DisplayName("備品を昇順に押さえるので、逆の順で送る2つのまとまりを、何度同時に動かしても、デッドロックしない")
	void reversedOrderDoesNotDeadlock() throws Exception {
		long e1 = equipment("BLOCK7");
		long e2 = equipment("BLOCK8");
		long e3 = equipment("BLOCK9");
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			for (int round = 0; round < 30; round++) {
				CountDownLatch start = new CountDownLatch(1);
				Future<?> a = executor.submit(() -> lockAll(start, List.of(e1, e2, e3)));
				Future<?> b = executor.submit(() -> lockAll(start, List.of(e3, e2, e1)));
				start.countDown();
				a.get(20, TimeUnit.SECONDS);
				b.get(20, TimeUnit.SECONDS);
			}
		} finally {
			executor.shutdownNow();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}

	/** 備品をまとめて押さえて、少し持つ（行き違いを起こしやすくする）。送る順は、わざと変える。 */
	private void lockAll(CountDownLatch start, List<Long> ids) {
		await(start);
		transaction.executeWithoutResult(status -> {
			equipmentCustomMapper.selectByIdsForUpdate(ids);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
	}

	/** 別の接続で、その行を NOWAIT で押さえてみる。押さえられれば true、押さえられない（3572）なら false。 */
	private boolean canLock(long id) {
		try {
			transaction.executeWithoutResult(status -> jdbc
					.queryForObject("SELECT id FROM equipment WHERE id = ? FOR UPDATE NOWAIT", Long.class, id));
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
