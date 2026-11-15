package jp.co.highjunk.equipment.api.auth.passwordtokens.use;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkIssuer;
import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.common.slack.SlackDirectMessage;
import jp.co.highjunk.equipment.common.slack.SlackSendResult;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * A-06 のDM（④）が送れなかったとき（詳細設計 auth/A-06_合言葉でパスワードを決める.md の11章、画面設計 9章の注意3）のテスト。
 * 送れなくても、パスワードの変更はそのままで、画面の返し方（200）は変わらない。
 */
@DisplayName("A-06 合言葉でパスワードを決める：DMが送れなかったとき")
class AuthPasswordTokensUseSlackFailureApiTest extends AuthApiTestBase {

	private static final String PREFIX = "http://localhost:8080/password-set?token=";

	private static final String NEW_PASSWORD = "Xq7#kd9!Lm2";

	/** テストの設定では、送り手は FakeSlackSender（テスト用の偽物の Bean）。送れなかった結果を返す偽物に置き換える。 */
	@MockitoBean
	FakeSlackSender slackSender;

	@Autowired
	PasswordLinkIssuer issuer;

	private void useWith(SlackSendResult result) throws Exception {
		when(slackSender.sendDirectMessage(eq(IPPAN), any(SlackDirectMessage.class))).thenReturn(result);
		String token = issuer.issue(userId(IPPAN), PasswordLinkPurpose.RESET).getLinkUrl().substring(PREFIX.length());

		mockMvc.perform(post("/api/auth/password-tokens/use")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + token + "\",\"password\":\"" + NEW_PASSWORD + "\",\"passwordConfirm\":\""
						+ NEW_PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"));

		verify(slackSender, times(1)).sendDirectMessage(eq(IPPAN), any(SlackDirectMessage.class));
		login(IPPAN, NEW_PASSWORD).andExpect(status().isOk());
		assertThat(jdbc.queryForObject("SELECT used_at FROM password_tokens WHERE user_id = ?", Object.class,
				userId(IPPAN))).isNotNull();
	}

	@Test
	@DisplayName("Slack のユーザーが見つからない（USER_NOT_FOUND）：パスワードは変わる。同じ 200")
	void userNotFound() throws Exception {
		useWith(SlackSendResult.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("そのほかの失敗（FAILED）：パスワードは変わる。同じ 200")
	void failed() throws Exception {
		useWith(SlackSendResult.FAILED);
	}
}
