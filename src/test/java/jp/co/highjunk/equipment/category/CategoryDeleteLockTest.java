package jp.co.highjunk.equipment.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
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

import jp.co.highjunk.equipment.category.service.CategoryDeleteService;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;

/**
 * K-04 の、同時に操作されたときの守り（詳細設計 category/K-04_種別を消す.md の8章）のテスト。
 * 備品を足す側（これから作る C-02 など）の決まりどおりに、種別の行を FOR SHARE で読んでから備品を足すまとまり（トランザクション）を
 * 本当に開いたままにし、その間に K-04 が消そうとすると、待たされ、足した備品を数えて 400 になることを確かめる。
 * 2つのまとまりを別々に確定させるので、このテストは @Transactional を付けず、最後に足した備品を消して元に戻す。
 */
@SpringBootTest
@DisplayName("K-04 種別を消す：同時に備品を足されたとき")
class CategoryDeleteLockTest {

	static final long MOUSE_ID = 3;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	TransactionTemplate transaction;

	@Autowired
	CategoryDeleteService categoryDeleteService;

	@AfterEach
	void cleanUp() {
		jdbc.update("DELETE FROM equipment WHERE management_no = 'LOCK001'");
		jdbc.update("UPDATE categories SET deleted_at = NULL, version = 0 WHERE id = ?", MOUSE_ID);
	}

	@Test
	@DisplayName("備品を足すまとまりが種別の行を読んでいる間、K-04 は待たされ、その備品が確定したあとで数えて 400（消さない）")
	void deleteWaitsForEquipmentInsert() throws Exception {
		long userId = jdbc.queryForObject("SELECT id FROM users WHERE email = 'soumu@example.com'", Long.class);
		CountDownLatch inserted = new CountDownLatch(1);
		CountDownLatch commit = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			// 備品を足す側：種別の行を FOR SHARE で読み、備品を足して、確定させずに待つ
			Future<?> insert = executor.submit(() -> transaction.executeWithoutResult(status -> {
				jdbc.queryForObject("SELECT id FROM categories WHERE id = ? AND deleted_at IS NULL FOR SHARE", Long.class,
						MOUSE_ID);
				jdbc.update("INSERT INTO equipment (management_no, category_id, status, created_at, updated_at)"
						+ " VALUES ('LOCK001', ?, 'MANAGED', NOW(), NOW())", MOUSE_ID);
				inserted.countDown();
				await(commit);
			}));
			assertThat(inserted.await(10, TimeUnit.SECONDS)).isTrue();

			// K-04：種別の行を FOR UPDATE で押さえようとして、待たされる
			Future<?> delete = executor.submit(() -> categoryDeleteService.delete(MOUSE_ID, userId));
			try {
				delete.get(1, TimeUnit.SECONDS);
				throw new AssertionError("K-04 が待たされずに終わった");
			} catch (TimeoutException expected) {
				// 待たされている
			}

			// 備品を足す側が確定すると、K-04 が動き、足された備品を数えて 400 になる
			commit.countDown();
			insert.get(10, TimeUnit.SECONDS);
			try {
				delete.get(10, TimeUnit.SECONDS);
				throw new AssertionError("K-04 が 400 にならなかった");
			} catch (java.util.concurrent.ExecutionException e) {
				assertThat(e.getCause()).isInstanceOf(BusinessRuleException.class)
						.hasMessage("この種別の備品があるため削除できません");
			}
			assertThat(jdbc.queryForObject("SELECT deleted_at IS NULL FROM categories WHERE id = ?", Boolean.class,
					MOUSE_ID)).isTrue();
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
