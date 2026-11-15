package jp.co.highjunk.equipment.api.auth.passwordresetrequests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.ResultActions;

import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkTokens;
import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.common.slack.SentSlackMessage;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * A-04 パスワード設定用のリンクの依頼（docs/API設計.md 3.1・4.3、詳細設計 auth/A-04_パスワード設定リンクの依頼.md。ZPOP-58）のテスト。
 */
@DisplayName("A-04 パスワード設定用のリンクの依頼")
class AuthPasswordResetRequestsApiTest extends AuthApiTestBase {

	@Autowired
	FakeSlackSender slack;

	@Autowired
	Clock clock;

	@BeforeEach
	void clearSlack() {
		slack.clear();
	}

	private ResultActions request(String email) throws Exception {
		return requestJson("{\"email\":\"" + email + "\"}");
	}

	private ResultActions requestJson(String json) throws Exception {
		return mockMvc.perform(post("/api/auth/password-reset-requests")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	private void expectAccepted(ResultActions result) throws Exception {
		result.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.status").value(202))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	private List<Map<String, Object>> tokens(String email) {
		return jdbc.queryForList("SELECT * FROM password_tokens WHERE user_id = ? ORDER BY id", userId(email));
	}

	@Test
	@DisplayName("登録されているメールアドレス：202。RESET のリンクを作り、本人にDM（①）を1通送る")
	void registered() throws Exception {
		expectAccepted(request(IPPAN));

		List<Map<String, Object>> tokens = tokens(IPPAN);
		assertThat(tokens).hasSize(1);
		assertThat(tokens.get(0).get("purpose")).isEqualTo("RESET");
		assertThat(tokens.get(0).get("used_at")).isNull();
		assertThat(tokens.get(0).get("revoked_at")).isNull();
		assertThat(jdbc.queryForObject("SELECT TIMESTAMPDIFF(MINUTE, created_at, expires_at) FROM password_tokens WHERE id = ?",
				Long.class, tokens.get(0).get("id"))).isEqualTo(30);

		List<SentSlackMessage> messages = slack.findAll();
		assertThat(messages).hasSize(1);
		SentSlackMessage dm = messages.get(0);
		assertThat(dm.getEmail()).isEqualTo(IPPAN);
		assertThat(dm.getTitle()).isEqualTo("【備品管理】パスワード再設定のご案内");
		assertThat(dm.getBody()).contains("30分").contains("無視してください").doesNotContain(PASSWORD);

		// DM のリンクの合言葉のハッシュが、保存したハッシュと同じ
		String url = dm.getBody().lines().filter(l -> l.contains("/password-set?token=")).findFirst().orElseThrow();
		String token = url.substring(url.indexOf("token=") + "token=".length()).trim();
		assertThat(PasswordLinkTokens.hash(token)).isEqualTo(tokens.get(0).get("token_hash"));
	}

	@Test
	@DisplayName("依頼しただけでは、パスワードは変わらない（ログインできる）。ユーザの行も書き換えない")
	void doesNotChangePassword() throws Exception {
		Map<String, Object> before = jdbc.queryForMap(
				"SELECT password_hash, failed_login_count, locked_at, session_valid_after, version, updated_at FROM users WHERE email = ?",
				IPPAN);

		expectAccepted(request(IPPAN));

		assertThat(jdbc.queryForMap(
				"SELECT password_hash, failed_login_count, locked_at, session_valid_after, version, updated_at FROM users WHERE email = ?",
				IPPAN)).isEqualTo(before);
		login(IPPAN, PASSWORD).andExpect(status().isOk());
	}

	@Test
	@DisplayName("登録のないメールアドレス：同じ 202・同じ文。リンクもDMも出ない")
	void unknownEmail() throws Exception {
		expectAccepted(request("nobody@example.com"));

		assertThat(slack.findAll()).isEmpty();
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM password_tokens", Integer.class)).isZero();
	}

	@Test
	@DisplayName("削除されたユーザ：同じ 202。リンクもDMも出ない")
	void deletedUser() throws Exception {
		change("UPDATE users SET deleted_at = " + NOW + " WHERE email = ?", IPPAN);

		expectAccepted(request(IPPAN));

		assertThat(slack.findAll()).isEmpty();
		assertThat(tokens(IPPAN)).isEmpty();
	}

	@Test
	@DisplayName("メールアドレスの大文字・小文字は区別しない。DMの宛先は、登録しているメールアドレス")
	void ignoresCase() throws Exception {
		expectAccepted(request("Ippan@Example.COM"));

		assertThat(tokens(IPPAN)).hasSize(1);
		assertThat(slack.findAll()).hasSize(1);
		assertThat(slack.findAll().get(0).getEmail()).isEqualTo(IPPAN);
	}

	@Test
	@DisplayName("5分以内にもう一度依頼：同じ 202。リンクもDMも増えない")
	void withinInterval() throws Exception {
		expectAccepted(request(IPPAN));
		expectAccepted(request(IPPAN));

		assertThat(tokens(IPPAN)).hasSize(1);
		assertThat(tokens(IPPAN).get(0).get("revoked_at")).isNull();
		assertThat(slack.findAll()).hasSize(1);
	}

	@Test
	@DisplayName("5分に1分足りない（4分59秒前）の依頼がある：まだ送らない（境界値）")
	void justBeforeInterval() throws Exception {
		insertResetToken(userId(IPPAN), 299);

		expectAccepted(request(IPPAN));

		assertThat(tokens(IPPAN)).hasSize(1);
		assertThat(slack.findAll()).isEmpty();
	}

	@Test
	@DisplayName("5分たった（5分1秒前）の依頼がある：新しいリンクを作ってDMを送り、前のリンクは無効になる（境界値）")
	void afterInterval() throws Exception {
		insertResetToken(userId(IPPAN), 301);

		expectAccepted(request(IPPAN));

		List<Map<String, Object>> tokens = tokens(IPPAN);
		assertThat(tokens).hasSize(2);
		assertThat(tokens.get(0).get("revoked_at")).isNotNull();
		assertThat(tokens.get(1).get("revoked_at")).isNull();
		assertThat(slack.findAll()).hasSize(1);
	}

	@Test
	@DisplayName("ほかの人への依頼は、5分の数に入らない")
	void otherUserIsIndependent() throws Exception {
		expectAccepted(request(IPPAN));
		expectAccepted(request(SOUMU));

		assertThat(tokens(IPPAN)).hasSize(1);
		assertThat(tokens(SOUMU)).hasSize(1);
		assertThat(slack.findAll()).hasSize(2);
	}

	@Test
	@DisplayName("ロックのリンク（LOCK）は、5分の数に入らない。ロックされた直後でも依頼でき、前のリンクは無効になる")
	void lockLinkDoesNotCount() throws Exception {
		for (int i = 0; i < 5; i++) {
			login(IPPAN, "wrong-password").andExpect(status().isUnauthorized());
		}
		assertThat(tokens(IPPAN)).hasSize(1);
		slack.clear();

		expectAccepted(request(IPPAN));

		List<Map<String, Object>> tokens = tokens(IPPAN);
		assertThat(tokens).hasSize(2);
		assertThat(tokens.get(0).get("purpose")).isEqualTo("LOCK");
		assertThat(tokens.get(0).get("revoked_at")).isNotNull();
		assertThat(tokens.get(1).get("purpose")).isEqualTo("RESET");
		assertThat(slack.findAll()).hasSize(1);
	}

	@Test
	@DisplayName("ロック中のユーザにも送る。パスワードがまだない人（最初の総務の担当者）にも送る")
	void lockedAndNoPasswordUsers() throws Exception {
		change("UPDATE users SET locked_at = " + NOW + " WHERE email = ?", IPPAN);

		expectAccepted(request(IPPAN));
		expectAccepted(request("admin@example.com"));

		assertThat(tokens(IPPAN)).hasSize(1);
		assertThat(tokens("admin@example.com")).hasSize(1);
		assertThat(slack.findAll()).hasSize(2);
	}

	@Test
	@DisplayName("ログインしていなくても呼べる（ログインしたセッションを付けない）")
	void noLoginNeeded() throws Exception {
		request(IPPAN).andExpect(status().isAccepted());
	}

	@Test
	@DisplayName("入力の誤り：メールアドレスが空なら 400。errors に画面設計の文")
	void blank() throws Exception {
		requestJson("{\"email\":\"\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("email"))
				.andExpect(jsonPath("$.errors[0].message").value("メールアドレスを入力してください"));
		requestJson("{}").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].message").value("メールアドレスを入力してください"));
		assertThat(slack.findAll()).isEmpty();
	}

	@Test
	@DisplayName("入力の誤り：メールアドレスの形でなければ 400")
	void badFormat() throws Exception {
		requestJson("{\"email\":\"not-an-email\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("email"))
				.andExpect(jsonPath("$.errors[0].message").value("メールアドレスの形が正しくありません"));
	}

	@Test
	@DisplayName("メールアドレスは254文字まで。254文字は入力の誤りにならず（登録がないので 202）、255文字は 400")
	void maxLength() throws Exception {
		expectAccepted(request(emailOfLength(254)));
		requestJson("{\"email\":\"" + emailOfLength(255) + "\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].message").value("メールアドレスの形が正しくありません"));
	}

	@Test
	@DisplayName("なりすまし対策の合言葉（CSRF）がなければ 403")
	void csrfRequired() throws Exception {
		mockMvc.perform(post("/api/auth/password-reset-requests")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + IPPAN + "\"}"))
				.andExpect(status().isForbidden());
		assertThat(slack.findAll()).isEmpty();
	}

	/** 何秒前に作った RESET のリンクを入れる。時刻は、アプリと同じ Clock から作る（データベースの時刻に頼らない）。 */
	private void insertResetToken(long userId, long secondsAgo) {
		LocalDateTime now = LocalDateTime.now(clock);
		change("INSERT INTO password_tokens (user_id, token_hash, purpose, expires_at, created_at) VALUES (?, ?, 'RESET', ?, ?)",
				userId, "b".repeat(64), now.plusDays(1), now.minusSeconds(secondsAgo));
	}
}
