package jp.co.highjunk.equipment.equipment;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.dto.equipment.EquipmentCreateRequest;
import jp.co.highjunk.equipment.equipment.service.EquipmentCreateService;

/**
 * C-03 の、同時に種別を消されたときの守り（詳細設計 equipment/C-03_備品を登録する.md の8章、category/category.md 3章の No.2）のテスト。
 * K-04 と同じように種別の行を FOR UPDATE で押さえて消すまとまりを本当に開いたままにし、その間に C-03 が同じ種別の備品を足そうとすると、
 * 待たされ、種別が消されたのを見て 400（種別を選んでください）になる（消された種別の備品ができない）ことを確かめる。
 * 2つのまとまりを別々に確定させるので、このテストは @Transactional を付けず、最後に元に戻す。
 */
@SpringBootTest
@DisplayName("C-03 備品を登録する：同時に種別を消されたとき")
class EquipmentCreateLockTest {

	static final long MOUSE_ID = 3;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	TransactionTemplate transaction;

	@Autowired
	EquipmentCreateService equipmentCreateService;

	@AfterEach
	void cleanUp() {
		jdbc.update("DELETE FROM equipment WHERE management_no = 'LOCK101'");
		jdbc.update("UPDATE categories SET deleted_at = NULL, version = 0 WHERE id = ?", MOUSE_ID);
	}

	@Test
	@DisplayName("種別を消すまとまりが種別の行を押さえている間、C-03 は待たされ、消されたのを見て 400（備品を足さない）")
	void createWaitsForCategoryDelete() throws Exception {
		long userId = jdbc.queryForObject("SELECT id FROM users WHERE email = 'soumu@example.com'", Long.class);
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch commit = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			// K-04 と同じ押さえ方：種別の行を FOR UPDATE で読み、消して、確定させずに待つ
			Future<?> delete = executor.submit(() -> transaction.executeWithoutResult(status -> {
				jdbc.queryForObject("SELECT id FROM categories WHERE id = ? AND deleted_at IS NULL FOR UPDATE", Long.class,
						MOUSE_ID);
				jdbc.update("UPDATE categories SET deleted_at = NOW(), version = version + 1 WHERE id = ?", MOUSE_ID);
				locked.countDown();
				await(commit);
			}));
			assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

			// C-03：種別の行を FOR SHARE で読もうとして、待たされる
			EquipmentCreateRequest request = new EquipmentCreateRequest("LOCK101", MOUSE_ID, null, null, null, null,
					null, null, null, null, null, null);
			Future<?> create = executor.submit(() -> equipmentCreateService.create(request, userId));
			try {
				create.get(1, TimeUnit.SECONDS);
				throw new AssertionError("C-03 が待たされずに終わった");
			} catch (TimeoutException expected) {
				// 待たされている
			}

			// 消す側が確定すると、C-03 が動き、種別が消されたのを見て 400 になる
			commit.countDown();
			delete.get(10, TimeUnit.SECONDS);
			try {
				create.get(10, TimeUnit.SECONDS);
				throw new AssertionError("C-03 が 400 にならなかった");
			} catch (ExecutionException e) {
				assertThat(e.getCause()).isInstanceOf(FieldErrorException.class);
				assertThat(((FieldErrorException) e.getCause()).getErrors().get(0).message())
						.isEqualTo("種別を選んでください");
			}
			assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM equipment WHERE management_no = 'LOCK101'",
					Integer.class)).isZero();
		} finally {
			commit.countDown();
			executor.shutdownNow();
		}
	}

	private static void await(CountDownLatch latch) {
		try {
			latch.await(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
