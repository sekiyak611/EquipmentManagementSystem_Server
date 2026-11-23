package jp.co.highjunk.equipment.dao.mapper.custom;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.dao.entity.Loan;

/**
 * LoanCustomMapper の、B-04 で足したメソッド selectCurrentByEquipmentIdsForUpdate（詳細設計 equipment/mine/B-04_確認した.md の7章 No.1）のテスト。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional）。行を押さえること（FOR UPDATE）は、CustomMapperBulkLockTest で確かめる。
 */
@SpringBootTest
@Transactional
@DisplayName("LoanCustomMapper#selectCurrentByEquipmentIdsForUpdate")
class LoanCustomMapperTest {

	@Autowired
	LoanCustomMapper loanCustomMapper;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	Clock clock;

	@Test
	@DisplayName("送った備品の、今借りている貸出履歴だけを、備品の ID の昇順で返す（返却済み・取り消し済み・送っていない備品の分は返さない）")
	void onlyCurrentLoansOfGivenEquipment() {
		LocalDate today = LocalDate.now(clock);
		long e1 = equipment("BM001");
		long e2 = equipment("BM002");
		long e3 = equipment("BM003");
		long eReturned = equipment("BM004");
		long eCancelled = equipment("BM005");
		long eNotAsked = equipment("BM006");
		long l2 = loan(e2, today.minusDays(1), null, false);
		long l1 = loan(e1, today.minusDays(2), null, false);
		loan(e3, today.minusDays(3), null, false); // e3 は、送らない（今借りているが、結果に出ない）
		loan(eReturned, today.minusDays(9), today.minusDays(5), false);
		loan(eCancelled, today.minusDays(9), null, true);
		loan(eNotAsked, today.minusDays(1), null, false);
		long eNoLoan = equipment("BM007");

		List<Loan> loans = loanCustomMapper
				.selectCurrentByEquipmentIdsForUpdate(List.of(eNoLoan, eCancelled, eReturned, e2, e1));

		assertThat(loans).extracting(Loan::getId).containsExactly(l1, l2);
		assertThat(loans).extracting(Loan::getEquipmentId).containsExactly(e1, e2);
		assertThat(loans).allSatisfy(loan -> {
			assertThat(loan.getReturnedOn()).isNull();
			assertThat(loan.getDeletedAt()).isNull();
			assertThat(loan.getUserId()).isNotNull();
			assertThat(loan.getVersion()).isEqualTo(0);
		});
	}

	@Test
	@DisplayName("同じ備品に、返却済みの履歴と今借りている履歴が両方あっても、今借りている1件だけを返す。同じ ID が重なって送られても、1件だけ")
	void returnedAndCurrentOfSameEquipment() {
		LocalDate today = LocalDate.now(clock);
		long e = equipment("BM020");
		loan(e, today.minusDays(30), today.minusDays(20), false);
		loan(e, today.minusDays(10), today.minusDays(5), false);
		long current = loan(e, today.minusDays(2), null, false);

		assertThat(loanCustomMapper.selectCurrentByEquipmentIdsForUpdate(List.of(e))).extracting(Loan::getId)
				.containsExactly(current);
		assertThat(loanCustomMapper.selectCurrentByEquipmentIdsForUpdate(List.of(e, e))).extracting(Loan::getId)
				.containsExactly(current);
	}

	@Test
	@DisplayName("備品を1件だけ送ったときも、今借りている貸出履歴を返す。借りられていない備品なら、空")
	void single() {
		LocalDate today = LocalDate.now(clock);
		long lent = equipment("BM010");
		long loanId = loan(lent, today, null, false);
		long free = equipment("BM011");
		assertThat(loanCustomMapper.selectCurrentByEquipmentIdsForUpdate(List.of(lent))).extracting(Loan::getId)
				.containsExactly(loanId);
		assertThat(loanCustomMapper.selectCurrentByEquipmentIdsForUpdate(List.of(free))).isEmpty();
	}

	@Test
	@DisplayName("備品の ID の一覧が空・null：空を返す（SQL の誤りにならない）")
	void emptyList() {
		assertThat(loanCustomMapper.selectCurrentByEquipmentIdsForUpdate(List.of())).isEmpty();
		assertThat(loanCustomMapper.selectCurrentByEquipmentIdsForUpdate(null)).isEmpty();
	}

	long equipment(String managementNo) {
		LocalDateTime now = LocalDateTime.now(clock);
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, created_at, updated_at)"
				+ " VALUES (?, 1, 'MANAGED', ?, ?)", managementNo, now, now);
		return jdbc.queryForObject("SELECT id FROM equipment WHERE management_no = ?", Long.class, managementNo);
	}

	long loan(long equipmentId, LocalDate lentOn, LocalDate returnedOn, boolean cancelled) {
		LocalDateTime now = LocalDateTime.now(clock);
		long userId = jdbc.queryForObject("SELECT id FROM users WHERE email = 'ippan@example.com'", Long.class);
		jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, returned_on, deleted_at, created_at, updated_at)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?)", equipmentId, userId, lentOn, returnedOn, cancelled ? now : null, now,
				now);
		return jdbc.queryForObject("SELECT MAX(id) FROM loans WHERE equipment_id = ?", Long.class, equipmentId);
	}
}
