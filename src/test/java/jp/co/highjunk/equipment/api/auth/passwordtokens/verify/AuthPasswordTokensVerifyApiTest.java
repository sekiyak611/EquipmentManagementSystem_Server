package jp.co.highjunk.equipment.api.auth.passwordtokens.verify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkIssuer;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkTokens;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * A-05 合言葉の確認（docs/API設計.md 3.1、詳細設計 auth/A-05_合言葉の確認.md。ZPOP-59）のテスト。
 */
@DisplayName("A-05 合言葉の確認")
class AuthPasswordTokensVerifyApiTest extends AuthApiTestBase {

	private static final String PREFIX = "http://localhost:8080/password-set?token=";

	private static final String UNUSABLE = "このリンクは使えません。有効期限が切れたか、すでに使われています。";

	@Autowired
	PasswordLinkIssuer issuer;

	@Autowired
	Clock clock;

	private String issue(String email, PasswordLinkPurpose purpose) {
		return issuer.issue(userId(email), purpose).getLinkUrl().substring(PREFIX.length());
	}

	private ResultActions verify(String token) throws Exception {
		return verifyJson("{\"token\":\"" + token + "\"}");
	}

	private ResultActions verifyJson(String json) throws Exception {
		return mockMvc.perform(post("/api/auth/password-tokens/verify")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	private void expectUnusable(ResultActions result) throws Exception {
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value(UNUSABLE))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@DisplayName("使える合言葉（再設定）：200。data に対象のメールアドレスと用途 RESET")
	void reset() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		verify(token)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data.email").value(IPPAN))
				.andExpect(jsonPath("$.data.purpose").value("RESET"))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@DisplayName("ロックのリンク・ユーザ登録のリンクも使える。用途 LOCK・REGISTER を返す")
	void lockAndRegister() throws Exception {
		verify(issue(IPPAN, PasswordLinkPurpose.LOCK))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.purpose").value("LOCK"));
		verify(issue(SOUMU, PasswordLinkPurpose.REGISTER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(SOUMU))
				.andExpect(jsonPath("$.data.purpose").value("REGISTER"));
	}

	@Test
	@DisplayName("開いただけでは使用済みにしない。何度確かめても、同じ答えで、used_at・revoked_at は空のまま")
	void doesNotConsume() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		verify(token).andExpect(status().isOk());
		verify(token).andExpect(status().isOk());

		Map<String, Object> row = jdbc.queryForMap("SELECT used_at, revoked_at FROM password_tokens WHERE user_id = ?",
				userId(IPPAN));
		assertThat(row.get("used_at")).isNull();
		assertThat(row.get("revoked_at")).isNull();
	}

	@Test
	@DisplayName("ログインしていなくても呼べる（ログインしたセッションを付けない）")
	void noLoginNeeded() throws Exception {
		verify(issue(IPPAN, PasswordLinkPurpose.RESET)).andExpect(status().isOk());
	}

	@Test
	@DisplayName("正しくない合言葉：400。「このリンクは使えません。…」。errors なし")
	void unknown() throws Exception {
		issue(IPPAN, PasswordLinkPurpose.RESET);

		expectUnusable(verify(PasswordLinkTokens.generate()));
	}

	@Test
	@DisplayName("合言葉が空・ない・形のちがう値：入力の誤りにせず、同じ 400・同じ文")
	void blankToken() throws Exception {
		expectUnusable(verifyJson("{\"token\":\"\"}"));
		expectUnusable(verifyJson("{\"token\":\"   \"}"));
		expectUnusable(verifyJson("{}"));
		expectUnusable(verifyJson("{\"token\":null}"));
	}

	@Test
	@DisplayName("使用済み：400。同じ文")
	void used() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);
		change("UPDATE password_tokens SET used_at = " + NOW + " WHERE user_id = ?", userId(IPPAN));

		expectUnusable(verify(token));
	}

	@Test
	@DisplayName("より新しいリンクが送られている：前の合言葉は 400・同じ文。新しいほうは 200")
	void olderLink() throws Exception {
		String older = issue(IPPAN, PasswordLinkPurpose.RESET);
		String newer = issue(IPPAN, PasswordLinkPurpose.RESET);

		expectUnusable(verify(older));
		verify(newer).andExpect(status().isOk());
	}

	@Test
	@DisplayName("期限切れ：400。同じ文")
	void expired() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);
		change("UPDATE password_tokens SET expires_at = ? WHERE user_id = ?",
				LocalDateTime.now(clock).minusMinutes(1), userId(IPPAN));

		expectUnusable(verify(token));
	}

	@Test
	@DisplayName("削除されたユーザのリンク：400。同じ文")
	void deletedUser() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);
		change("UPDATE users SET deleted_at = " + NOW + " WHERE email = ?", IPPAN);

		expectUnusable(verify(token));
	}

	@Test
	@DisplayName("なりすまし対策の合言葉（CSRF）がなければ 403")
	void csrfRequired() throws Exception {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		mockMvc.perform(post("/api/auth/password-tokens/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + token + "\"}"))
				.andExpect(status().isForbidden());
	}
}
