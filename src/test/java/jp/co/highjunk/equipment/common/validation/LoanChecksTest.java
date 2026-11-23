package jp.co.highjunk.equipment.common.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.common.error.ConflictException;
import jp.co.highjunk.equipment.common.error.ForbiddenException;
import jp.co.highjunk.equipment.common.error.NotFoundException;
import jp.co.highjunk.equipment.common.validation.ServiceCheckOrder.First;
import jp.co.highjunk.equipment.common.validation.ServiceCheckOrder.Second;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 貸出履歴のチェック（LoanExists・LoanBorrowedBySelf・LoanNotReturned・LoanEquipmentManaged。詳細設計 common.md 3.20）を、
 * ServiceInDtoChecker 経由で、DB を使って確かめるテスト。B-03 の ServiceInDto と同じ付け方をしたテスト用の入れ物を使う。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional）。
 */
@SpringBootTest
@Transactional
@DisplayName("貸出履歴のチェック（common.validation）")
class LoanChecksTest {

	static final String IPPAN = "ippan@example.com";

	static final String SOUMU = "soumu@example.com";

	@Autowired
	ServiceInDtoChecker checker;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	Clock clock;

	@Test
	@DisplayName("自分が借りていて、まだ返していない、管理中の備品：守れている（何も起きない）")
	void allPass() {
		long loanId = loan(userId(IPPAN), "MANAGED", null);
		assertThatCode(() -> checker.check(dto(loanId, userId(IPPAN)))).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("貸出履歴がない：404（NotFoundException）")
	void notFound() {
		assertThatThrownBy(() -> checker.check(dto(999_999_999L, userId(IPPAN)))).isInstanceOf(NotFoundException.class);
	}

	@Test
	@DisplayName("取り消された貸出履歴：404（ないものとして扱う）")
	void cancelled() {
		long loanId = loan(userId(IPPAN), "MANAGED", null);
		jdbc.update("UPDATE loans SET deleted_at = ? WHERE id = ?", LocalDateTime.now(clock), loanId);
		assertThatThrownBy(() -> checker.check(dto(loanId, userId(IPPAN)))).isInstanceOf(NotFoundException.class);
	}

	@Test
	@DisplayName("ほかの人が借りている：403（文は「ほかの人が借りているため、返却はできません。」）")
	void notMine() {
		long loanId = loan(userId(SOUMU), "MANAGED", null);
		assertThatThrownBy(() -> checker.check(dto(loanId, userId(IPPAN))))
				.isInstanceOfSatisfying(ForbiddenException.class,
						e -> assertThat(e.getMessage()).isEqualTo("ほかの人が借りているため、返却はできません。"));
	}

	@Test
	@DisplayName("すでに返却されている：409（文は「この備品はすでに返却されています」）")
	void alreadyReturned() {
		long loanId = loan(userId(IPPAN), "MANAGED", LocalDate.now(clock));
		assertThatThrownBy(() -> checker.check(dto(loanId, userId(IPPAN))))
				.isInstanceOfSatisfying(ConflictException.class,
						e -> assertThat(e.getMessage()).isEqualTo("この備品はすでに返却されています"));
	}

	@Test
	@DisplayName("備品が管理中でない（廃棄・所在不明）：400（文は「この備品は返却できません。総務へお問い合わせください。」）")
	void equipmentNotManaged() {
		for (String status : new String[] { "DISPOSED", "MISSING" }) {
			long loanId = loan(userId(IPPAN), status, null);
			assertThatThrownBy(() -> checker.check(dto(loanId, userId(IPPAN))))
					.isInstanceOfSatisfying(BusinessRuleException.class,
							e -> assertThat(e.getMessage()).isEqualTo("この備品は返却できません。総務へお問い合わせください。"));
		}
	}

	@Test
	@DisplayName("備品が削除されている：管理中でないものとして 400")
	void equipmentDeleted() {
		long loanId = loan(userId(IPPAN), "MANAGED", null);
		long equipmentId = jdbc.queryForObject("SELECT equipment_id FROM loans WHERE id = ?", Long.class, loanId);
		jdbc.update("UPDATE equipment SET deleted_at = ? WHERE id = ?", LocalDateTime.now(clock), equipmentId);
		assertThatThrownBy(() -> checker.check(dto(loanId, userId(IPPAN)))).isInstanceOf(BusinessRuleException.class);
	}

	@Test
	@DisplayName("ほかの人が借りていて、しかも返却済み：403（403 を 409 より先に返す）")
	void forbiddenBeforeConflict() {
		long loanId = loan(userId(SOUMU), "MANAGED", LocalDate.now(clock));
		assertThatThrownBy(() -> checker.check(dto(loanId, userId(IPPAN)))).isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("返却済みで、しかも備品が管理中でない：409（409 を 400 より先に返す）")
	void conflictBeforeBusinessRule() {
		long loanId = loan(userId(IPPAN), "DISPOSED", LocalDate.now(clock));
		assertThatThrownBy(() -> checker.check(dto(loanId, userId(IPPAN)))).isInstanceOf(ConflictException.class);
	}

	@Test
	@DisplayName("ほかの人が借りていて、しかも備品が管理中でない：403（403 を 400 より先に返す）")
	void forbiddenBeforeBusinessRule() {
		long loanId = loan(userId(SOUMU), "DISPOSED", null);
		assertThatThrownBy(() -> checker.check(dto(loanId, userId(IPPAN)))).isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("ログインしている人の ID が null：自分の貸出履歴ではないとして 403（安全側）")
	void loginUserNull() {
		long loanId = loan(userId(IPPAN), "MANAGED", null);
		assertThatThrownBy(() -> checker.check(dto(loanId, null))).isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("LoanExists（forUpdate なし）：ある貸出履歴は守れている。ない・取り消し済みは守れない")
	void existsWithoutForUpdate() {
		long loanId = loan(userId(IPPAN), "MANAGED", null);
		assertThatCode(() -> checker.check(new ExistsOnlyDto(loanId))).doesNotThrowAnyException();
		assertThatThrownBy(() -> checker.check(new ExistsOnlyDto(999_999_999L))).isInstanceOf(NotFoundException.class);
		// 取り消し済み（同じ ID を続けて読むと、MyBatis が同じ transaction の中で結果を覚えているので、別の貸出履歴で確かめる）
		long cancelled = loan(userId(IPPAN), "MANAGED", null);
		jdbc.update("UPDATE loans SET deleted_at = ? WHERE id = ?", LocalDateTime.now(clock), cancelled);
		assertThatThrownBy(() -> checker.check(new ExistsOnlyDto(cancelled))).isInstanceOf(NotFoundException.class);
	}

	@Test
	@DisplayName("loanId が null：チェックしない（何も起きない）")
	void nullLoanId() {
		assertThatCode(() -> checker.check(dto(null, userId(IPPAN)))).doesNotThrowAnyException();
	}

	long userId(String email) {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
	}

	/** 備品と貸出履歴を1つずつ入れ、貸出履歴の ID を返す（返却日があれば返却済み） */
	long loan(long userId, String equipmentStatus, LocalDate returnedOn) {
		long categoryId = jdbc.queryForObject("SELECT id FROM categories WHERE deleted_at IS NULL ORDER BY id LIMIT 1",
				Long.class);
		LocalDateTime now = LocalDateTime.now(clock);
		jdbc.update("INSERT INTO equipment (management_no, category_id, maker, model, status, created_at, updated_at)"
				+ " VALUES (?, ?, 'テスト', 'T-1', ?, ?, ?)", "LC-" + System.nanoTime(), categoryId, equipmentStatus, now,
				now);
		long equipmentId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
		LocalDate today = LocalDate.now(clock);
		jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, due_on, returned_on, created_at, updated_at)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?)", equipmentId, userId, today.minusDays(3), today.plusDays(10), returnedOn,
				now, now);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	static ReturnDto dto(Long loanId, Long loginUserId) {
		return new ReturnDto(loanId, loginUserId);
	}

	/** B-03 の ServiceInDto と同じ付け方をした、テスト用の入れ物 */
	@Getter
	@AllArgsConstructor
	@LoanBorrowedBySelf(groups = Second.class, payload = ServiceErrorType.Forbidden.class)
	@LoanNotReturned(groups = Second.class, payload = ServiceErrorType.Conflict.class)
	@LoanEquipmentManaged(groups = Second.class, payload = ServiceErrorType.BusinessRule.class)
	static class ReturnDto
			implements LoanBorrowedBySelf.Target, LoanNotReturned.Target, LoanEquipmentManaged.Target {

		@LoanExists(forUpdate = true, groups = First.class, payload = ServiceErrorType.NotFound.class)
		private final Long loanId;

		private final Long loginUserId;
	}

	/** LoanExists（forUpdate なし）だけを付けた、テスト用の入れ物 */
	@Getter
	@AllArgsConstructor
	static class ExistsOnlyDto {

		@LoanExists(groups = First.class, payload = ServiceErrorType.NotFound.class)
		private final Long loanId;
	}
}
