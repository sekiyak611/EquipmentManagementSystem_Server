package jp.co.highjunk.equipment.api.equipment.mine.search.item;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.calendar.ReminderCalendar;
import jp.co.highjunk.equipment.common.security.LoginUser;

/**
 * B-01 自分が借りている備品（docs/API設計.md 3.2・4.5、詳細設計 equipment/mine/B-01_自分の備品の一覧.md）のテスト。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional）。今日は、アプリの時計（Clock）の今日。
 * 一般社員 ippan@example.com・総務 soumu@example.com は、どちらも LOAN_SELF を持つ。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("B-01 自分が借りている備品")
class EquipmentMineSearchItemApiTest {

	static final String SOUMU = "soumu@example.com";

	static final String IPPAN = "ippan@example.com";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	Clock clock;

	@Autowired
	ReminderCalendar reminderCalendar;

	LocalDate today;

	LocalDate firstOfMonth;

	LocalDate lastMonth;

	@BeforeEach
	void setUp() {
		today = LocalDate.now(clock);
		firstOfMonth = today.withDayOfMonth(1);
		lastMonth = firstOfMonth.minusDays(1);
	}

	@Test
	@DisplayName("ログインしていない：401")
	void notLoggedIn() throws Exception {
		mockMvc.perform(get("/api/me/loans")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("LOAN_SELF がない：403")
	void forbidden() throws Exception {
		long roleId = insertRoleWithoutPermission();
		long userId = insertUser("nopermission@example.com", roleId);
		mockMvc.perform(get("/api/me/loans").with(loginAs(userId)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("この操作を行う権限がありません。"));
	}

	@Test
	@DisplayName("借りている備品がない：200。items は空、unconfirmedCount は 0。催促の日時は返す")
	void noLoans() throws Exception {
		search(IPPAN)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.errors").value(nullValue()))
				.andExpect(jsonPath("$.data.unconfirmedCount").value(0))
				.andExpect(jsonPath("$.data.items", hasSize(0)))
				.andExpect(jsonPath("$.data.reminderAt").isString())
				.andExpect(jsonPath("$.data.reminderPassed").isBoolean());
	}

	@Test
	@DisplayName("借りている備品の1件の形：貸出履歴のID・備品のID・管理番号・種別・メーカー・モデル・貸出日・返却予定日・期限超過・最終確認日・今月確認済みか")
	void itemShape() throws Exception {
		long equipmentId = equipment("A001", "MANAGED", 1, "Lenovo", "ThinkPad X1", lastMonth);
		long loanId = loan(equipmentId, userId(IPPAN), today.minusDays(3), today.plusDays(10), null);

		search(IPPAN)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items", hasSize(1)))
				.andExpect(jsonPath("$.data.items[0].loanId").value(loanId))
				.andExpect(jsonPath("$.data.items[0].equipmentId").value(equipmentId))
				.andExpect(jsonPath("$.data.items[0].managementNo").value("A001"))
				.andExpect(jsonPath("$.data.items[0].categoryName").value("PC"))
				.andExpect(jsonPath("$.data.items[0].maker").value("Lenovo"))
				.andExpect(jsonPath("$.data.items[0].model").value("ThinkPad X1"))
				.andExpect(jsonPath("$.data.items[0].lentOn").value(today.minusDays(3).toString()))
				.andExpect(jsonPath("$.data.items[0].dueOn").value(today.plusDays(10).toString()))
				.andExpect(jsonPath("$.data.items[0].overdue").value(false))
				.andExpect(jsonPath("$.data.items[0].lastConfirmedOn").value(lastMonth.toString()))
				.andExpect(jsonPath("$.data.items[0].confirmedThisMonth").value(false));
	}

	@Test
	@DisplayName("メーカー・モデル・返却予定日・最終確認日が空なら null")
	void nullItems() throws Exception {
		long equipmentId = equipment("A002", "MANAGED", 2, null, null, null);
		loan(equipmentId, userId(IPPAN), today, null, null);

		search(IPPAN)
				.andExpect(jsonPath("$.data.items[0].categoryName").value("ヘッドセット"))
				.andExpect(jsonPath("$.data.items[0].maker").value(nullValue()))
				.andExpect(jsonPath("$.data.items[0].model").value(nullValue()))
				.andExpect(jsonPath("$.data.items[0].dueOn").value(nullValue()))
				.andExpect(jsonPath("$.data.items[0].lastConfirmedOn").value(nullValue()))
				.andExpect(jsonPath("$.data.items[0].overdue").value(false))
				.andExpect(jsonPath("$.data.items[0].confirmedThisMonth").value(false));
	}

	@Test
	@DisplayName("今月確認済みか：最終確認日が今月の1日・今日なら true、先月末・未記入なら false")
	void confirmedThisMonth() throws Exception {
		long ippan = userId(IPPAN);
		loan(equipment("A001", "MANAGED", 1, null, null, firstOfMonth), ippan, today, null, null);
		loan(equipment("A002", "MANAGED", 1, null, null, today), ippan, today, null, null);
		loan(equipment("A003", "MANAGED", 1, null, null, lastMonth), ippan, today, null, null);
		loan(equipment("A004", "MANAGED", 1, null, null, null), ippan, today, null, null);

		search(IPPAN)
				.andExpect(jsonPath("$.data.unconfirmedCount").value(2))
				// 未確認（A003・A004。貸出日が同じなので貸出履歴の順）を上に、確認済みを下に
				.andExpect(jsonPath("$.data.items[0].managementNo").value("A003"))
				.andExpect(jsonPath("$.data.items[1].managementNo").value("A004"))
				.andExpect(jsonPath("$.data.items[2].managementNo").value("A001"))
				.andExpect(jsonPath("$.data.items[3].managementNo").value("A002"))
				.andExpect(jsonPath("$.data.items[0].confirmedThisMonth").value(false))
				.andExpect(jsonPath("$.data.items[2].confirmedThisMonth").value(true))
				.andExpect(jsonPath("$.data.items[3].confirmedThisMonth").value(true));
	}

	@Test
	@DisplayName("並び順：未確認を上にし、その中は貸出日の古い順。確認済みの中も貸出日の古い順")
	void order() throws Exception {
		long ippan = userId(IPPAN);
		loan(equipment("U-NEW", "MANAGED", 1, null, null, lastMonth), ippan, today.minusDays(1), null, null);
		loan(equipment("C-OLD", "MANAGED", 1, null, null, today), ippan, today.minusDays(100), null, null);
		loan(equipment("U-OLD", "MANAGED", 1, null, null, null), ippan, today.minusDays(50), null, null);
		loan(equipment("C-NEW", "MANAGED", 1, null, null, today), ippan, today.minusDays(2), null, null);

		search(IPPAN)
				.andExpect(jsonPath("$.data.items[0].managementNo").value("U-OLD"))
				.andExpect(jsonPath("$.data.items[1].managementNo").value("U-NEW"))
				.andExpect(jsonPath("$.data.items[2].managementNo").value("C-OLD"))
				.andExpect(jsonPath("$.data.items[3].managementNo").value("C-NEW"));
	}

	@Test
	@DisplayName("期限超過：返却予定日が昨日なら true、今日・明日・なしなら false")
	void overdue() throws Exception {
		long ippan = userId(IPPAN);
		loan(equipment("A001", "MANAGED", 1, null, null, today), ippan, today.minusDays(40), today.minusDays(1), null);
		loan(equipment("A002", "MANAGED", 1, null, null, today), ippan, today.minusDays(30), today, null);
		loan(equipment("A003", "MANAGED", 1, null, null, today), ippan, today.minusDays(20), today.plusDays(1), null);
		loan(equipment("A004", "MANAGED", 1, null, null, today), ippan, today.minusDays(10), null, null);

		search(IPPAN)
				.andExpect(jsonPath("$.data.items[0].managementNo").value("A001"))
				.andExpect(jsonPath("$.data.items[0].overdue").value(true))
				.andExpect(jsonPath("$.data.items[1].overdue").value(false))
				.andExpect(jsonPath("$.data.items[2].overdue").value(false))
				.andExpect(jsonPath("$.data.items[3].overdue").value(false));
	}

	@Test
	@DisplayName("自分の分だけ：ほかの人の貸出・返却済み・取り消された貸出は出ない")
	void onlyMine() throws Exception {
		long ippan = userId(IPPAN);
		long soumu = userId(SOUMU);
		loan(equipment("MINE", "MANAGED", 1, null, null, null), ippan, today, null, null);
		loan(equipment("OTHER", "MANAGED", 1, null, null, null), soumu, today, null, null);
		loan(equipment("RETURNED", "MANAGED", 1, null, null, null), ippan, today.minusDays(5), null, today.minusDays(1));
		long cancelled = loan(equipment("CANCELLED", "MANAGED", 1, null, null, null), ippan, today, null, null);
		jdbc.update("UPDATE loans SET deleted_at = NOW() WHERE id = ?", cancelled);

		search(IPPAN)
				.andExpect(jsonPath("$.data.items", hasSize(1)))
				.andExpect(jsonPath("$.data.items[0].managementNo").value("MINE"))
				.andExpect(jsonPath("$.data.unconfirmedCount").value(1));
		// 総務の側からは、総務の分だけ
		search(SOUMU)
				.andExpect(jsonPath("$.data.items", hasSize(1)))
				.andExpect(jsonPath("$.data.items[0].managementNo").value("OTHER"));
	}

	@Test
	@DisplayName("管理中でない（不明）備品・削除された備品は出ない。管理中のものだけを数える")
	void onlyManaged() throws Exception {
		long ippan = userId(IPPAN);
		loan(equipment("OK", "MANAGED", 1, null, null, null), ippan, today, null, null);
		loan(equipment("MISSING", "MISSING", 1, null, null, null), ippan, today, null, null);
		long deleted = equipment("DELETED", "MANAGED", 1, null, null, null);
		loan(deleted, ippan, today, null, null);
		jdbc.update("UPDATE equipment SET deleted_at = NOW() WHERE id = ?", deleted);

		search(IPPAN)
				.andExpect(jsonPath("$.data.items", hasSize(1)))
				.andExpect(jsonPath("$.data.items[0].managementNo").value("OK"))
				.andExpect(jsonPath("$.data.unconfirmedCount").value(1));
	}

	@Test
	@DisplayName("すべて確認済み：unconfirmedCount は 0")
	void allConfirmed() throws Exception {
		long ippan = userId(IPPAN);
		loan(equipment("A001", "MANAGED", 1, null, null, today), ippan, today, null, null);
		loan(equipment("A002", "MANAGED", 1, null, null, firstOfMonth), ippan, today, null, null);

		search(IPPAN)
				.andExpect(jsonPath("$.data.unconfirmedCount").value(0))
				.andExpect(jsonPath("$.data.items", hasSize(2)));
	}

	@Test
	@DisplayName("催促の日時は、今月の催促日の時刻（日本時間の +09:00 の形）。過ぎたかは、今の時計で決まる")
	void reminder() throws Exception {
		// 設定の催促の時刻は毎朝9:00（application.yml）。「2026-11-25T09:00:00+09:00」の形（秒まで。+09:00）
		String expectedDate = reminderCalendar.reminderDate(YearMonth.from(today)).toString();
		search(IPPAN)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.reminderAt").value(expectedDate + "T09:00:00+09:00"))
				.andExpect(jsonPath("$.data.reminderPassed").value(reminderCalendar.isReminderPassed()));
	}

	// ここから下は、テストの道具

	ResultActions search(String email) throws Exception {
		return mockMvc.perform(get("/api/me/loans").with(login(email)));
	}

	RequestPostProcessor login(String email) {
		return loginAs(userId(email));
	}

	RequestPostProcessor loginAs(long userId) {
		return authentication(new UsernamePasswordAuthenticationToken(new LoginUser(userId, LocalDateTime.now()),
				null, List.of()));
	}

	long userId(String email) {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
	}

	/** 権限項目を1つも持たない権限（ロール）を作り、ID を返す */
	long insertRoleWithoutPermission() {
		jdbc.update("INSERT INTO roles (name, is_default, created_at, updated_at) VALUES ('権限なし', FALSE, NOW(), NOW())");
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	long insertUser(String email, long roleId) {
		jdbc.update("INSERT INTO users (name, email, role_id, created_at, updated_at) VALUES ('権限なしの人', ?, ?, NOW(), NOW())",
				email, roleId);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	/** 備品を1つ入れ、ID を返す（種別は ID で決める。1＝PC、2＝ヘッドセット） */
	long equipment(String managementNo, String status, long categoryId, String maker, String model,
			LocalDate lastConfirmedOn) {
		jdbc.update("INSERT INTO equipment (management_no, category_id, maker, model, status, last_confirmed_on,"
				+ " created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())", managementNo, categoryId, maker,
				model, status, lastConfirmedOn);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	/** 貸出履歴を1つ入れ、ID を返す（返却日があれば返却済み） */
	long loan(long equipmentId, long userId, LocalDate lentOn, LocalDate dueOn, LocalDate returnedOn) {
		jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, due_on, returned_on, created_at, updated_at)"
				+ " VALUES (?, ?, ?, ?, ?, NOW(), NOW())", equipmentId, userId, lentOn, dueOn, returnedOn);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}
}
