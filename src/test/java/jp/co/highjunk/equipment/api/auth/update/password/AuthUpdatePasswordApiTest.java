package jp.co.highjunk.equipment.api.auth.update.password;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.common.slack.SentSlackMessage;
import jp.co.highjunk.equipment.dao.mapper.custom.UserCustomMapper;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * A-07 自分のパスワード変更（docs/API設計.md 3.1、詳細設計 auth/A-07_自分のパスワード変更.md。ZPOP-61）のテスト。
 */
@DisplayName("A-07 自分のパスワード変更")
class AuthUpdatePasswordApiTest extends AuthApiTestBase {

	private static final String NEW_PASSWORD = "Xq7#kd9!Lm2";

	private static final String WRONG_CURRENT = "現在のパスワードが違います";

	private static final String BLOCKED = "このパスワードはよく使われているため、使えません。別のパスワードにしてください";

	@Autowired
	FakeSlackSender slack;

	@Autowired
	Clock clock;

	@Autowired
	UserCustomMapper userCustomMapper;

	@BeforeEach
	void clearSlack() {
		slack.clear();
	}

	private ResultActions update(MockHttpSession session, String current, String password, String confirm)
			throws Exception {
		return updateJson(session, "{\"currentPassword\":" + quote(current) + ",\"password\":" + quote(password)
				+ ",\"passwordConfirm\":" + quote(confirm) + "}");
	}

	private ResultActions update(MockHttpSession session, String current, String password) throws Exception {
		return update(session, current, password, password);
	}

	private ResultActions updateJson(MockHttpSession session, String json) throws Exception {
		var request = put("/api/me/password")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json);
		if (session != null) {
			request.session(session);
		}
		return mockMvc.perform(request);
	}

	private static String quote(String value) {
		return value == null ? "null" : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

	private String passwordHash(String email) {
		return jdbc.queryForObject("SELECT password_hash FROM users WHERE email = ?", String.class, email);
	}

	private int failedCount(String email) {
		return jdbc.queryForObject("SELECT failed_login_count FROM users WHERE email = ?", Integer.class, email);
	}

	private void expectCurrentError(ResultActions result, String message) throws Exception {
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("currentPassword"))
				.andExpect(jsonPath("$.errors[0].message").value(message));
	}

	private void expectPasswordError(ResultActions result, String message) throws Exception {
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("password"))
				.andExpect(jsonPath("$.errors[0].message").value(message));
	}

	@Test
	@DisplayName("変えられる：200（data は null）。新しいパスワードでログインでき、古いパスワードではできない")
	void success() throws Exception {
		MockHttpSession session = loginSession(IPPAN);

		update(session, PASSWORD, NEW_PASSWORD)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));

		login(IPPAN, NEW_PASSWORD).andExpect(status().isOk());
		login(IPPAN, PASSWORD).andExpect(status().isUnauthorized());
		assertThat(passwordHash(IPPAN)).startsWith("$2").doesNotContain(NEW_PASSWORD);
	}

	@Test
	@DisplayName("この端末はログインしたまま（次の操作も使える）。なりすまし対策の合言葉（XSRF-TOKEN）は新しくなる")
	void thisDeviceStaysLoggedIn() throws Exception {
		MockHttpSession session = loginSession(IPPAN);

		var result = update(session, PASSWORD, NEW_PASSWORD).andExpect(status().isOk()).andReturn();

		assertThat(result.getResponse().getCookie("XSRF-TOKEN")).isNotNull();
		me(session).andExpect(status().isOk());
	}

	@Test
	@DisplayName("この端末以外のログインを切る：前にログインしていたほかの端末は、次の操作で 401")
	void cutsOtherDevices() throws Exception {
		MockHttpSession otherDevice = loginSession(IPPAN);
		me(otherDevice).andExpect(status().isOk());
		// session_valid_after は秒まで。ほかの端末のログインが、前の秒になるように待つ（A-06 の12章 No.1）
		Thread.sleep(1100);
		MockHttpSession thisDevice = loginSession(IPPAN);

		update(thisDevice, PASSWORD, NEW_PASSWORD).andExpect(status().isOk());

		me(otherDevice).andExpect(status().isUnauthorized());
		me(thisDevice).andExpect(status().isOk());
	}

	@Test
	@DisplayName("パスワードが変わったことを、本人にDM（④）で知らせる。パスワードは書かない")
	void sendsChangedDm() throws Exception {
		MockHttpSession session = loginSession(IPPAN);

		update(session, PASSWORD, NEW_PASSWORD).andExpect(status().isOk());

		List<SentSlackMessage> messages = slack.findAll();
		assertThat(messages).hasSize(1);
		assertThat(messages.get(0).getEmail()).isEqualTo(IPPAN);
		assertThat(messages.get(0).getTitle()).isEqualTo("【備品管理】パスワードを変更しました");
		assertThat(messages.get(0).getBody()).contains("変更した日時：").doesNotContain(NEW_PASSWORD)
				.doesNotContain(PASSWORD);
	}

	@Test
	@DisplayName("updated_by は本人、version は増えない、session_valid_after は秒までにそろえた今")
	void updatesColumns() throws Exception {
		Integer version = jdbc.queryForObject("SELECT version FROM users WHERE email = ?", Integer.class, IPPAN);
		MockHttpSession session = loginSession(IPPAN);
		LocalDateTime start = LocalDateTime.now(clock).withNano(0);

		update(session, PASSWORD, NEW_PASSWORD).andExpect(status().isOk());

		Map<String, Object> row = jdbc.queryForMap("SELECT updated_by, version FROM users WHERE email = ?", IPPAN);
		LocalDateTime validAfter = jdbc.queryForObject("SELECT session_valid_after FROM users WHERE email = ?",
				LocalDateTime.class, IPPAN);
		assertThat(row.get("updated_by")).isEqualTo(userId(IPPAN));
		assertThat(row.get("version")).isEqualTo(version);
		assertThat(validAfter).isAfterOrEqualTo(start).isBeforeOrEqualTo(LocalDateTime.now(clock));
	}

	@Test
	@DisplayName("変えるのは、ログインしている本人のパスワードだけ（ほかの人のパスワードは変わらない）")
	void changesOnlyOwnPassword() throws Exception {
		String ippanHash = passwordHash(IPPAN);
		MockHttpSession session = loginSession(SOUMU);

		update(session, PASSWORD, NEW_PASSWORD).andExpect(status().isOk());

		assertThat(passwordHash(IPPAN)).isEqualTo(ippanHash);
		login(SOUMU, NEW_PASSWORD).andExpect(status().isOk());
		login(IPPAN, PASSWORD).andExpect(status().isOk());
	}

	@Test
	@DisplayName("成功すると、失敗の回数が0に戻る（ロックされていないとき）")
	void successResetsFailedCount() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		change("UPDATE users SET failed_login_count = 3 WHERE email = ?", IPPAN);

		update(session, PASSWORD, NEW_PASSWORD).andExpect(status().isOk());

		assertThat(failedCount(IPPAN)).isZero();
	}

	@Test
	@DisplayName("現在のパスワードが違う：400。currentPassword に「現在のパスワードが違います」。パスワードは変わらず、回数が1増える。DMなし。この端末はそのまま")
	void wrongCurrent() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		String hash = passwordHash(IPPAN);

		expectCurrentError(update(session, "wrong-password", NEW_PASSWORD), WRONG_CURRENT);

		assertThat(passwordHash(IPPAN)).isEqualTo(hash);
		assertThat(failedCount(IPPAN)).isEqualTo(1);
		assertThat(slack.findAll()).isEmpty();
		me(session).andExpect(status().isOk());
	}

	@Test
	@DisplayName("現在のパスワードを4回間違えても、まだロックしない（境界値）。そのあと正しく変えると、回数は0に戻る")
	void fourWrongThenSuccess() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		for (int i = 0; i < 4; i++) {
			update(session, "wrong-password", NEW_PASSWORD).andExpect(status().isBadRequest());
		}
		assertThat(failedCount(IPPAN)).isEqualTo(4);
		assertThat(jdbc.queryForObject("SELECT locked_at FROM users WHERE email = ?", Object.class, IPPAN)).isNull();

		update(session, PASSWORD, NEW_PASSWORD).andExpect(status().isOk());

		assertThat(failedCount(IPPAN)).isZero();
	}

	@Test
	@DisplayName("5回続けて間違えたらロック：5回目も同じ 400。DM（③）を送り、LOCK のリンクを作り、この端末をログアウトさせる")
	void fifthWrongLocksAndLogsOut() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		for (int i = 0; i < 4; i++) {
			update(session, "wrong-password", NEW_PASSWORD).andExpect(status().isBadRequest());
		}

		expectCurrentError(update(session, "wrong-password", NEW_PASSWORD), WRONG_CURRENT);

		assertThat(failedCount(IPPAN)).isEqualTo(5);
		assertThat(jdbc.queryForObject("SELECT locked_at FROM users WHERE email = ?", Object.class, IPPAN)).isNotNull();
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM password_tokens WHERE user_id = ? AND purpose = 'LOCK'",
				Integer.class, userId(IPPAN))).isEqualTo(1);
		assertThat(slack.findAll()).hasSize(1);
		assertThat(slack.findAll().get(0).getTitle()).isEqualTo("【備品管理】アカウントをロックしました");
		me(session).andExpect(status().isUnauthorized());
		login(IPPAN, PASSWORD).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("ロック中に現在のパスワードを間違えても、数えない・DMを送らない。この端末はそのまま。同じ 400")
	void wrongWhileLocked() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		change("UPDATE users SET locked_at = " + NOW + ", failed_login_count = 5 WHERE email = ?", IPPAN);

		expectCurrentError(update(session, "wrong-password", NEW_PASSWORD), WRONG_CURRENT);

		assertThat(failedCount(IPPAN)).isEqualTo(5);
		assertThat(slack.findAll()).isEmpty();
		me(session).andExpect(status().isOk());
	}

	@Test
	@DisplayName("ロック中でも、正しい現在のパスワードなら変えられる。ロックは解除しない（失敗の回数もそのまま）")
	void changeWhileLocked() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		change("UPDATE users SET locked_at = " + NOW + ", failed_login_count = 5 WHERE email = ?", IPPAN);

		update(session, PASSWORD, NEW_PASSWORD).andExpect(status().isOk());

		assertThat(jdbc.queryForObject("SELECT locked_at FROM users WHERE email = ?", Object.class, IPPAN)).isNotNull();
		assertThat(failedCount(IPPAN)).isEqualTo(5);
		login(IPPAN, NEW_PASSWORD).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("新しいパスワードが現在と同じ：400。password に「現在と同じパスワードは使えません」。回数は数えない")
	void sameAsCurrent() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		String hash = passwordHash(IPPAN);

		expectPasswordError(update(session, PASSWORD, PASSWORD), "現在と同じパスワードは使えません");

		assertThat(passwordHash(IPPAN)).isEqualTo(hash);
		assertThat(failedCount(IPPAN)).isZero();
		assertThat(slack.findAll()).isEmpty();
	}

	@Test
	@DisplayName("新しいパスワードが、よく使われるもの・自分のメールアドレス・システム名と同じ：400。password に画面設計 8章の文。回数は数えない")
	void blockedNewPassword() throws Exception {
		MockHttpSession session = loginSession(IPPAN);

		for (String password : List.of("qwertyuiop", "IPPAN@example.com", "EquipmentManagement")) {
			expectPasswordError(update(session, PASSWORD, password), BLOCKED);
		}

		assertThat(failedCount(IPPAN)).isZero();
		me(session).andExpect(status().isOk());
	}

	@Test
	@DisplayName("現在のパスワードが違い、新しいパスワードも使えないとき：currentPassword のエラーだけ（回数は数える）")
	void wrongCurrentComesFirst() throws Exception {
		MockHttpSession session = loginSession(IPPAN);

		expectCurrentError(update(session, "wrong-password", "qwertyuiop"), WRONG_CURRENT);

		assertThat(failedCount(IPPAN)).isEqualTo(1);
	}

	@Test
	@DisplayName("入力の誤り：現在のパスワードが空・ない：400「現在のパスワードを入力してください」。回数は数えない")
	void blankCurrent() throws Exception {
		MockHttpSession session = loginSession(IPPAN);

		expectCurrentError(update(session, "", NEW_PASSWORD), "現在のパスワードを入力してください");
		expectCurrentError(update(session, null, NEW_PASSWORD), "現在のパスワードを入力してください");
		expectCurrentError(updateJson(session, "{\"password\":\"" + NEW_PASSWORD + "\",\"passwordConfirm\":\""
				+ NEW_PASSWORD + "\"}"), "現在のパスワードを入力してください");

		assertThat(failedCount(IPPAN)).isZero();
	}

	@Test
	@DisplayName("入力の誤り：新しいパスワードが空・短い・長い・使えない文字：400。password の1件だけ。回数は数えない")
	void passwordFormat() throws Exception {
		MockHttpSession session = loginSession(IPPAN);

		expectPasswordError(update(session, PASSWORD, "", ""), "新しいパスワードを入力してください");
		expectPasswordError(update(session, PASSWORD, "Xq7#kd9"), "パスワードは8文字以上で入力してください");
		expectPasswordError(update(session, PASSWORD, "Xq7#kd9!".repeat(8) + "a"), "パスワードは64文字以内で入力してください");
		expectPasswordError(update(session, PASSWORD, "パスワードパスワード"),
				"パスワードに使えるのは、半角の英字・数字・記号・スペースです");

		assertThat(failedCount(IPPAN)).isZero();
	}

	@Test
	@DisplayName("入力の誤り：確認が違う・空・ない：400。passwordConfirm に「確認用のパスワードが一致しません」")
	void confirmMismatch() throws Exception {
		MockHttpSession session = loginSession(IPPAN);

		for (String confirm : new String[] { "Xq7#kd9!Lm3", "", null }) {
			update(session, PASSWORD, NEW_PASSWORD, confirm)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors", hasSize(1)))
					.andExpect(jsonPath("$.errors[0].field").value("passwordConfirm"))
					.andExpect(jsonPath("$.errors[0].message").value("確認用のパスワードが一致しません"));
		}
		assertThat(failedCount(IPPAN)).isZero();
	}

	@Test
	@DisplayName("入力の誤りが先：現在のパスワードが違い、確認も違うとき：確認のエラーを返し、回数は数えない")
	void formatCheckComesFirst() throws Exception {
		MockHttpSession session = loginSession(IPPAN);

		update(session, "wrong-password", NEW_PASSWORD, "different")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("passwordConfirm"));

		assertThat(failedCount(IPPAN)).isZero();
	}

	@Test
	@DisplayName("64文字・スペースを含むパスワードも、そのまま変えられる")
	void longPasswordWithSpaces() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		String password = " " + "Xq7#kd9!".repeat(7) + "Xq7 kd9";

		update(session, PASSWORD, password).andExpect(status().isOk());

		login(IPPAN, password).andExpect(status().isOk());
	}

	@Test
	@DisplayName("ログインしていない：401")
	void notLoggedIn() throws Exception {
		updateJson(null, "{\"currentPassword\":\"" + PASSWORD + "\",\"password\":\"" + NEW_PASSWORD
				+ "\",\"passwordConfirm\":\"" + NEW_PASSWORD + "\"}")
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("ログインしてください。"));
		assertThat(slack.findAll()).isEmpty();
	}

	@Test
	@DisplayName("なりすまし対策の合言葉（CSRF）がなければ 403")
	void csrfRequired() throws Exception {
		MockHttpSession session = loginSession(IPPAN);

		mockMvc.perform(put("/api/me/password")
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"currentPassword\":\"" + PASSWORD + "\",\"password\":\"" + NEW_PASSWORD
						+ "\",\"passwordConfirm\":\"" + NEW_PASSWORD + "\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("読んだあとにパスワードがほかで変わっていたら、書き換えない（0件）。読んだハッシュと同じなら、1件書き換える")
	void staleHashIsNotOverwritten() {
		String hash = passwordHash(IPPAN);
		long id = userId(IPPAN);
		LocalDateTime now = LocalDateTime.now(clock);

		assertThat(userCustomMapper.changePassword(id, "$2a$10$stale-hash", "new-hash", now.withNano(0), now))
				.isZero();
		assertThat(passwordHash(IPPAN)).isEqualTo(hash);

		assertThat(userCustomMapper.changePassword(id, hash, "new-hash", now.withNano(0), now)).isEqualTo(1);
		sqlSession.clearCache();
		assertThat(passwordHash(IPPAN)).isEqualTo("new-hash");
	}
}
