package jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.security.LoginUser;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * C-06 備品を「確認した」にする（docs/API設計.md 3.3、詳細設計 equipment/management/C-06_確認した.md）のテスト。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional）。今日は、アプリの時計（Clock）の今日。
 * 総務 soumu@example.com は CONFIRM_ALL を持ち、一般社員 ippan@example.com は持たない。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("C-06 確認した（総務）")
class EquipmentManagementUpdateConfirmationItemApiTest {

	static final String SOUMU = "soumu@example.com";

	static final String IPPAN = "ippan@example.com";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	Clock clock;

	LocalDate today;

	LocalDate lastMonth;

	@BeforeEach
	void setUp() {
		today = LocalDate.now(clock);
		lastMonth = today.withDayOfMonth(1).minusDays(1);
	}

	@Test
	@DisplayName("ログインしていない：401")
	void notLoggedIn() throws Exception {
		long id = equipment("A001", "MANAGED", lastMonth);
		mockMvc.perform(post("/api/equipment/{id}/confirmation", id).with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("CONFIRM_ALL がない（一般社員）：403。変えない")
	void forbidden() throws Exception {
		long id = equipment("A001", "MANAGED", lastMonth);
		confirm(IPPAN, id)
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("この操作を行う権限がありません。"));
		assertThat(row(id).get("last_confirmed_on").toString()).isEqualTo(lastMonth.toString());
	}

	@Test
	@DisplayName("CSRF の合言葉がない：403")
	void noCsrf() throws Exception {
		long id = equipment("A001", "MANAGED", lastMonth);
		mockMvc.perform(post("/api/equipment/{id}/confirmation", id).with(login(SOUMU)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("保管中の備品：200。最終確認日を今日にし、version を1増やす。data は C-01 の1件と同じ形")
	void inStock() throws Exception {
		long id = equipment("A001", "MANAGED", lastMonth);
		confirm(SOUMU, id)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data.length()").value(10))
				.andExpect(jsonPath("$.data.id").value(id))
				.andExpect(jsonPath("$.data.managementNo").value("A001"))
				.andExpect(jsonPath("$.data.categoryName").value("PC"))
				.andExpect(jsonPath("$.data.status").value("MANAGED"))
				.andExpect(jsonPath("$.data.loanStatus").value("IN_STOCK"))
				.andExpect(jsonPath("$.data.borrowerName").value(nullValue()))
				.andExpect(jsonPath("$.data.lastConfirmedOn").value(today.toString()))
				.andExpect(jsonPath("$.data.confirmedThisMonth").value(true))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		Map<String, Object> row = row(id);
		assertThat(row.get("last_confirmed_on").toString()).isEqualTo(today.toString());
		assertThat(row.get("version")).isEqualTo(1);
		assertThat(row.get("updated_by")).isEqualTo(userId(SOUMU));
	}

	@Test
	@DisplayName("貸出中の備品も確認できる。data に借用者の氏名が入る")
	void lent() throws Exception {
		long id = equipment("A001", "MANAGED", null);
		jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
				id, userId(IPPAN), lastMonth);
		confirm(SOUMU, id)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.loanStatus").value("LENT"))
				.andExpect(jsonPath("$.data.borrowerName").value("開発 一般"))
				.andExpect(jsonPath("$.data.lastConfirmedOn").value(today.toString()));
	}

	@Test
	@DisplayName("今月すでに確認済みでも誤りにせず、今日にする（version も1増やす）")
	void alreadyConfirmed() throws Exception {
		long id = equipment("A001", "MANAGED", today.withDayOfMonth(1));
		confirm(SOUMU, id).andExpect(status().isOk());
		assertThat(row(id).get("last_confirmed_on").toString()).isEqualTo(today.toString());
		assertThat(row(id).get("version")).isEqualTo(1);
	}

	@ParameterizedTest
	@ValueSource(strings = { "MISSING", "DISPOSED" })
	@DisplayName("管理中でない（不明・廃棄）：400「管理中の備品だけ確認できます」。変えない")
	void notManaged(String status) throws Exception {
		long id = equipment("A001", status, lastMonth);
		confirm(SOUMU, id)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("管理中の備品だけ確認できます"))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(row(id).get("last_confirmed_on").toString()).isEqualTo(lastMonth.toString());
		assertThat(row(id).get("version")).isEqualTo(0);
	}

	@Test
	@DisplayName("ない備品・削除された備品：404「対象が見つかりません。」")
	void notFound() throws Exception {
		confirm(SOUMU, 999999)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"));
		long id = equipment("A001", "MANAGED", lastMonth);
		jdbc.update("UPDATE equipment SET deleted_at = NOW() WHERE id = ?", id);
		confirm(SOUMU, id).andExpect(status().isNotFound());
		assertThat(row(id).get("last_confirmed_on").toString()).isEqualTo(lastMonth.toString());
	}

	@Test
	@DisplayName("ID が数でない：400、errors は null")
	void badId() throws Exception {
		mockMvc.perform(post("/api/equipment/abc/confirmation").with(login(SOUMU))
				.with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	// ここから下は、テストの道具

	ResultActions confirm(String email, long id) throws Exception {
		return mockMvc.perform(post("/api/equipment/{id}/confirmation", id).with(login(email))
				.with(CsrfTestHelper.csrfToken(mockMvc)));
	}

	RequestPostProcessor login(String email) {
		return authentication(new UsernamePasswordAuthenticationToken(new LoginUser(userId(email), LocalDateTime.now()),
				null, List.of()));
	}

	long userId(String email) {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
	}

	Map<String, Object> row(long id) {
		return jdbc.queryForMap("SELECT * FROM equipment WHERE id = ?", id);
	}

	/** 備品を1つ入れ、ID を返す（種別は PC。廃棄なら廃棄日も入れる） */
	long equipment(String managementNo, String status, LocalDate lastConfirmedOn) {
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, disposed_on, last_confirmed_on,"
				+ " created_at, updated_at) VALUES (?, 1, ?, ?, ?, NOW(), NOW())", managementNo, status,
				"DISPOSED".equals(status) ? lastMonth : null, lastConfirmedOn);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}
}
