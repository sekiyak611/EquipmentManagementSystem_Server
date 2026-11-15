package jp.co.highjunk.equipment.api.auth.passwordtokens.use;

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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkIssuer;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkTokens;
import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.common.slack.SentSlackMessage;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * A-06 合言葉でパスワードを決める（docs/API設計.md 3.1・4.4、詳細設計 auth/A-06_合言葉でパスワードを決める.md。ZPOP-60）のテスト。
 */
@DisplayName("A-06 合言葉でパスワードを決める")
class AuthPasswordTokensUseApiTest extends AuthApiTestBase {

	private static final String PREFIX = "http://localhost:8080/password-set?token=";

	private static final String UNUSABLE = "このリンクは使えません。有効期限が切れたか、すでに使われています。";

	private static final String NEW_PASSWORD = "Xq7#kd9!Lm2";

	@Autowired
	PasswordLinkIssuer issuer;

	@Autowired
	FakeSlackSender slack;

	@Autowired
	Clock clock;

	@BeforeEach
	void clearSlack() {
		slack.clear();
	}

	private String issue(String email, PasswordLinkPurpose purpose) {
		return issuer.issue(userId(email), purpose).getLinkUrl().substring(PREFIX.length());
	}

	private ResultActions use(String token, String password, String confirm) throws Exception {
		return useJson("{\"token\":" + quote(token) + ",\"password\":" + quote(password) + ",\"passwordConfirm\":"
				+ quote(confirm) + "}");
	}

	private ResultActions use(String token, String password) throws Exception {
		return use(token, password, password);
	}

	private ResultActions useJson(String json) throws Exception {
		return mockMvc.perform(post("/api/auth/password-tokens/use")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	private static String quote(String value) {
		return value == null ? "null" : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

	private String passwordHash(String email) {
		return jdbc.queryForObject("SELECT password_hash FROM users WHERE email = ?", String.class, email);
	}

	private void expectUnusable(ResultActions result) throws Exception {
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(UNUSABLE))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@DisplayName("再設定のリンクで決める：200（data は null）。新しいパスワードでログインでき、古いパスワードではできない")
	void reset() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		use(token, NEW_PASSWORD)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));

		login(IPPAN, NEW_PASSWORD).andExpect(status().isOk());
		login(IPPAN, PASSWORD).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("パスワードそのものは保存しない（bcrypt のハッシュ）。リンクは使用済み（used_at）になる")
	void storesHashAndMarksUsed() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		use(token, NEW_PASSWORD).andExpect(status().isOk());

		assertThat(passwordHash(IPPAN)).startsWith("$2").doesNotContain(NEW_PASSWORD);
		Map<String, Object> row = jdbc.queryForMap("SELECT used_at, revoked_at FROM password_tokens WHERE user_id = ?",
				userId(IPPAN));
		assertThat(row.get("used_at")).isNotNull();
		assertThat(row.get("revoked_at")).isNull();
	}

	@Test
	@DisplayName("ロックを解除し、失敗の回数を0に戻す")
	void unlocksAccount() throws Exception {
		change("UPDATE users SET locked_at = " + NOW + ", failed_login_count = 5 WHERE email = ?", IPPAN);
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		use(token, NEW_PASSWORD).andExpect(status().isOk());

		Map<String, Object> row = jdbc.queryForMap("SELECT locked_at, failed_login_count FROM users WHERE email = ?",
				IPPAN);
		assertThat(row.get("locked_at")).isNull();
		assertThat(row.get("failed_login_count")).isEqualTo(0);
		login(IPPAN, NEW_PASSWORD).andExpect(status().isOk());
	}

	@Test
	@DisplayName("ロックの流れ全体：5回間違えてロック → DM（③）のリンクで決める → ロックが解け、ログインできる")
	void lockFlow() throws Exception {
		for (int i = 0; i < 5; i++) {
			login(IPPAN, "wrong-password").andExpect(status().isUnauthorized());
		}
		login(IPPAN, PASSWORD).andExpect(status().isUnauthorized());
		SentSlackMessage lockDm = slack.findAll().get(0);
		String url = lockDm.getBody().lines().filter(l -> l.contains("/password-set?token=")).findFirst().orElseThrow();
		String token = url.substring(url.indexOf("token=") + "token=".length()).trim();
		slack.clear();

		use(token, NEW_PASSWORD).andExpect(status().isOk());

		login(IPPAN, NEW_PASSWORD).andExpect(status().isOk());
	}

	@Test
	@DisplayName("自動ではログインさせない：ログイン状態（セッション）を渡さない")
	void doesNotLogin() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		MockHttpSession session = (MockHttpSession) use(token, NEW_PASSWORD).andExpect(status().isOk()).andReturn()
				.getRequest().getSession(false);

		assertThat(session).isNull();
	}

	@Test
	@DisplayName("すべての端末のログインを切る：前にログインしていた端末は、次の操作で 401。決めたあとのログインは使える")
	void cutsOldSessions() throws Exception {
		MockHttpSession oldSession = loginSession(IPPAN);
		me(oldSession).andExpect(status().isOk());
		// session_valid_after は秒まで。決める前のログインが、前の秒になるように待つ（A-06 の12章 No.1）
		Thread.sleep(1100);
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		use(token, NEW_PASSWORD).andExpect(status().isOk());

		me(oldSession).andExpect(status().isUnauthorized());
		// 決めた直後（同じ1秒の中）にログインしても、すぐには切られない
		MockHttpSession newSession = (MockHttpSession) login(IPPAN, NEW_PASSWORD).andExpect(status().isOk())
				.andReturn().getRequest().getSession(false);
		me(newSession).andExpect(status().isOk());
	}

	@Test
	@DisplayName("session_valid_after には、今を秒までにそろえて入る。updated_by は本人、version は増えない")
	void updatesColumns() throws Exception {
		Map<String, Object> before = jdbc.queryForMap("SELECT version FROM users WHERE email = ?", IPPAN);
		LocalDateTime start = LocalDateTime.now(clock).withNano(0);
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		use(token, NEW_PASSWORD).andExpect(status().isOk());

		Map<String, Object> row = jdbc.queryForMap("SELECT updated_by, version FROM users WHERE email = ?", IPPAN);
		LocalDateTime validAfter = jdbc.queryForObject("SELECT session_valid_after FROM users WHERE email = ?",
				LocalDateTime.class, IPPAN);
		assertThat(validAfter).isAfterOrEqualTo(start).isBeforeOrEqualTo(LocalDateTime.now(clock));
		assertThat(row.get("updated_by")).isEqualTo(userId(IPPAN));
		assertThat(row.get("version")).isEqualTo(before.get("version"));
	}

	@Test
	@DisplayName("再設定：パスワードが変わったことを、本人にDM（④）で知らせる。パスワードは書かない")
	void sendsChangedDm() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		use(token, NEW_PASSWORD).andExpect(status().isOk());

		List<SentSlackMessage> messages = slack.findAll();
		assertThat(messages).hasSize(1);
		assertThat(messages.get(0).getEmail()).isEqualTo(IPPAN);
		assertThat(messages.get(0).getTitle()).isEqualTo("【備品管理】パスワードを変更しました");
		assertThat(messages.get(0).getBody()).contains("変更した日時：").doesNotContain(NEW_PASSWORD).doesNotContain(token);
	}

	@Test
	@DisplayName("ユーザ登録のリンク（REGISTER）：パスワードがまだない人が、最初のパスワードを決められる。DMは送らない")
	void register() throws Exception {
		String token = issue("admin@example.com", PasswordLinkPurpose.REGISTER);

		use(token, NEW_PASSWORD).andExpect(status().isOk());

		assertThat(slack.findAll()).isEmpty();
		login("admin@example.com", NEW_PASSWORD).andExpect(status().isOk());
	}

	@Test
	@DisplayName("ロックのリンク（LOCK）でも決められる。DM（④）は送る")
	void lockLink() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.LOCK);

		use(token, NEW_PASSWORD).andExpect(status().isOk());

		assertThat(slack.findAll()).hasSize(1);
	}

	@Test
	@DisplayName("ログインしていなくても呼べる")
	void noLoginNeeded() throws Exception {
		use(issue(IPPAN, PasswordLinkPurpose.RESET), NEW_PASSWORD).andExpect(status().isOk());
	}

	@Test
	@DisplayName("同じリンクを2回使えない：2回目は 400（使えないリンク）。パスワードは1回目のまま")
	void cannotReuse() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);
		use(token, NEW_PASSWORD).andExpect(status().isOk());
		String hash = passwordHash(IPPAN);

		expectUnusable(use(token, "Another-Pass-9"));

		assertThat(passwordHash(IPPAN)).isEqualTo(hash);
		assertThat(slack.findAll()).hasSize(1);
	}

	@Test
	@DisplayName("使えない合言葉（ない・空・ない値・より新しいリンクがある・期限切れ・削除されたユーザ）：400。同じ文。パスワードは変わらない")
	void unusableTokens() throws Exception {
		String hash = passwordHash(IPPAN);

		expectUnusable(use(PasswordLinkTokens.generate(), NEW_PASSWORD));
		expectUnusable(use("", NEW_PASSWORD));
		expectUnusable(use(null, NEW_PASSWORD));
		expectUnusable(useJson("{\"password\":\"" + NEW_PASSWORD + "\",\"passwordConfirm\":\"" + NEW_PASSWORD + "\"}"));

		String older = issue(IPPAN, PasswordLinkPurpose.RESET);
		issue(IPPAN, PasswordLinkPurpose.RESET);
		expectUnusable(use(older, NEW_PASSWORD));

		String expired = issue(IPPAN, PasswordLinkPurpose.RESET);
		change("UPDATE password_tokens SET expires_at = ? WHERE token_hash = ?", LocalDateTime.now(clock).minusSeconds(1),
				PasswordLinkTokens.hash(expired));
		expectUnusable(use(expired, NEW_PASSWORD));

		String deleted = issue(SOUMU, PasswordLinkPurpose.RESET);
		change("UPDATE users SET deleted_at = " + NOW + " WHERE email = ?", SOUMU);
		expectUnusable(use(deleted, NEW_PASSWORD));

		assertThat(passwordHash(IPPAN)).isEqualTo(hash);
		assertThat(slack.findAll()).isEmpty();
	}

	@Test
	@DisplayName("使えないパスワード（よく使われる）：400。password に画面設計 8章の文。リンクは使用済みにならず、別のパスワードで決め直せる")
	void commonPassword() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);
		String hash = passwordHash(IPPAN);

		use(token, "qwertyuiop")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("password"))
				.andExpect(jsonPath("$.errors[0].message")
						.value("このパスワードはよく使われているため、使えません。別のパスワードにしてください"));

		assertThat(passwordHash(IPPAN)).isEqualTo(hash);
		assertThat(jdbc.queryForObject("SELECT used_at FROM password_tokens WHERE user_id = ?", Object.class,
				userId(IPPAN))).isNull();
		use(token, NEW_PASSWORD).andExpect(status().isOk());
	}

	@Test
	@DisplayName("使えないパスワード：大文字・小文字がちがっても、自分のメールアドレス・システム名は使えない")
	void emailAndSystemName() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		for (String password : List.of("ippan@example.com", "IPPAN@Example.com", "EquipmentManagement",
				"equipmentmanagementsystem")) {
			use(token, password)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors[0].field").value("password"))
					.andExpect(jsonPath("$.errors[0].message")
							.value("このパスワードはよく使われているため、使えません。別のパスワードにしてください"));
		}
		// ほかの人のメールアドレスは使える
		use(token, SOUMU).andExpect(status().isOk());
	}

	@Test
	@DisplayName("入力の誤り：空は 400。errors は password の1件だけ（確認も空なら一致する）")
	void blankPassword() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		use(token, "", "")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("password"))
				.andExpect(jsonPath("$.errors[0].message").value("新しいパスワードを入力してください"));
		use(token, null, null)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].message").value("新しいパスワードを入力してください"));
	}

	@Test
	@DisplayName("入力の誤り：7文字は短い、65文字は長い、日本語は使えない文字。どれも1つだけ")
	void passwordFormat() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		use(token, "Xq7#kd9").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].message").value("パスワードは8文字以上で入力してください"));
		use(token, "Xq7#kd9!".repeat(8) + "a").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].message").value("パスワードは64文字以内で入力してください"));
		use(token, "パスワードパスワード").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].message").value("パスワードに使えるのは、半角の英字・数字・記号・スペースです"));
		// 境界：8文字と64文字は決められる
		use(token, "Xq7#kd9!").andExpect(status().isOk());
	}

	@Test
	@DisplayName("64文字のパスワードで決められ、そのパスワードでログインできる")
	void maxLengthPassword() throws Exception {
		String password = "Xq7#kd9!".repeat(8);
		use(issue(IPPAN, PasswordLinkPurpose.RESET), password).andExpect(status().isOk());

		login(IPPAN, password).andExpect(status().isOk());
	}

	@Test
	@DisplayName("スペースを含むパスワードは、前後も取らずに、そのまま決められる")
	void passwordWithSpaces() throws Exception {
		String password = "  Xq7 kd9!  ";
		use(issue(IPPAN, PasswordLinkPurpose.RESET), password).andExpect(status().isOk());

		login(IPPAN, password).andExpect(status().isOk());
		login(IPPAN, password.strip()).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("入力の誤り：確認が違う・空・ないときは、passwordConfirm に「確認用のパスワードが一致しません」")
	void confirmMismatch() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		for (String confirm : new String[] { "Xq7#kd9!Lm3", "", null }) {
			use(token, NEW_PASSWORD, confirm)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors", hasSize(1)))
					.andExpect(jsonPath("$.errors[0].field").value("passwordConfirm"))
					.andExpect(jsonPath("$.errors[0].message").value("確認用のパスワードが一致しません"));
		}
		assertThat(jdbc.queryForObject("SELECT used_at FROM password_tokens WHERE user_id = ?", Object.class,
				userId(IPPAN))).isNull();
	}

	@Test
	@DisplayName("入力の誤り：パスワードの形と確認の違いが同時にあれば、2つとも errors に出る")
	void bothErrors() throws Exception {
		use(issue(IPPAN, PasswordLinkPurpose.RESET), "short", "different")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(2)));
	}

	@Test
	@DisplayName("入力の誤りが先：パスワードが誤りで、合言葉も使えないときは、入力の誤り（400・errors あり）を返す")
	void formatCheckComesFirst() throws Exception {
		use(PasswordLinkTokens.generate(), "short")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.errors", hasSize(1)));
	}

	@Test
	@DisplayName("なりすまし対策の合言葉（CSRF）がなければ 403")
	void csrfRequired() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		mockMvc.perform(post("/api/auth/password-tokens/use")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + token + "\",\"password\":\"" + NEW_PASSWORD + "\",\"passwordConfirm\":\""
						+ NEW_PASSWORD + "\"}"))
				.andExpect(status().isForbidden());
	}
}
