package jp.co.highjunk.equipment.api.auth.passwordresetrequests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.common.slack.SlackDirectMessage;
import jp.co.highjunk.equipment.common.slack.SlackSendResult;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * A-04 のDMが送れなかったとき（詳細設計 auth/A-04_パスワード設定リンクの依頼.md の5章 No.4、画面設計 5章・9章の注意3）のテスト。
 * 送れなくても、画面の返し方（202・同じ文）は変わらず、リンクは作ったまま。
 */
@DisplayName("A-04 パスワード設定用のリンクの依頼：DMが送れなかったとき")
class AuthPasswordResetRequestsSlackFailureApiTest extends AuthApiTestBase {

	/** テストの設定では、送り手は FakeSlackSender（テスト用の偽物の Bean）。送れなかった結果を返す偽物に置き換える。 */
	@MockitoBean
	FakeSlackSender slackSender;

	private void requestWith(SlackSendResult result) throws Exception {
		when(slackSender.sendDirectMessage(eq(IPPAN), any(SlackDirectMessage.class))).thenReturn(result);
		mockMvc.perform(post("/api/auth/password-reset-requests")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + IPPAN + "\"}"))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data").value(nullValue()));

		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM password_tokens WHERE user_id = ? AND purpose = 'RESET'",
				Integer.class, userId(IPPAN))).isEqualTo(1);
		verify(slackSender, times(1)).sendDirectMessage(eq(IPPAN), any(SlackDirectMessage.class));
	}

	@Test
	@DisplayName("Slack のユーザーが見つからない（USER_NOT_FOUND）：同じ 202・同じ文")
	void userNotFound() throws Exception {
		requestWith(SlackSendResult.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("そのほかの失敗（FAILED）：同じ 202・同じ文")
	void failed() throws Exception {
		requestWith(SlackSendResult.FAILED);
	}
}
