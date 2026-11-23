package jp.co.highjunk.equipment.testsupport;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * api.equipment.mine の API の、同時に操作されたときのテストで共通に使う値と道具（B-04 の LockTest が継ぐ）。
 * 2つのまとまり（トランザクション）を別々に確定させるので、@Transactional を付けない（EquipmentMineApiTestBase とは別）。
 * 入れた行は、管理番号の頭（prefix）で見つけて、テストが終わるたびに消す（元に戻す）。
 */
@SpringBootTest
public abstract class EquipmentMineLockTestBase {

	@Autowired
	protected JdbcTemplate jdbc;

	@Autowired
	protected TransactionTemplate transaction;

	@Autowired
	protected Clock clock;

	/** このテストが入れる備品の管理番号の頭（ほかのテストと重ならない名前にする） */
	protected abstract String prefix();

	/** 日付が変わる直前（深夜0時の5秒前から）なら、0時を過ぎるまで待つ（テストの中で今日の日付を取る間に、日付が変わらないように） */
	@BeforeEach
	void waitIfNearMidnight() throws InterruptedException {
		LocalTime now = LocalTime.now(clock);
		if (now.isAfter(LocalTime.of(23, 59, 55))) {
			Thread.sleep(Duration.between(now, LocalTime.MAX).toMillis() + 1500);
		}
	}

	@AfterEach
	void cleanUpRows() {
		jdbc.update("DELETE FROM loans WHERE equipment_id IN (SELECT id FROM equipment WHERE management_no LIKE ?)",
				prefix() + "%");
		jdbc.update("DELETE FROM equipment WHERE management_no LIKE ?", prefix() + "%");
	}

	protected long ippanId() {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class,
				EquipmentMineApiTestBase.IPPAN);
	}

	protected long equipmentIdOf(long loanId) {
		return jdbc.queryForObject("SELECT equipment_id FROM loans WHERE id = ?", Long.class, loanId);
	}

	/** 備品を1つ入れ、備品の ID を返す（管理中。借りられていない） */
	protected long newEquipment(String managementNo) {
		LocalDateTime now = LocalDateTime.now(clock);
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, created_at, updated_at)"
				+ " VALUES (?, 1, 'MANAGED', ?, ?)", managementNo, now, now);
		return jdbc.queryForObject("SELECT id FROM equipment WHERE management_no = ?", Long.class, managementNo);
	}

	/** 一般社員が借りている、管理中の備品の貸出履歴を入れ、貸出履歴の ID を返す（管理番号で、入れた行を引く） */
	protected long newLoan(String managementNo) {
		long equipmentId = newEquipment(managementNo);
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate today = now.toLocalDate();
		jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
				equipmentId, ippanId(), today.minusDays(2), now, now);
		return jdbc.queryForObject("SELECT id FROM loans WHERE equipment_id = ?", Long.class, equipmentId);
	}

	/** 動いているまとまりを止め、終わるのを待つ（残ったまとまりが、後始末の削除や、次のテストの邪魔をしないため） */
	protected static void stop(ExecutorService executor) throws InterruptedException {
		executor.shutdownNow();
		executor.awaitTermination(10, TimeUnit.SECONDS);
	}

	protected static void await(CountDownLatch latch) {
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
