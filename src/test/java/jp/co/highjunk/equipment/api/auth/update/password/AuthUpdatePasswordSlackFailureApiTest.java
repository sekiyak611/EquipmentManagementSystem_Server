package jp.co.highjunk.equipment.api.auth.update.password;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.common.slack.SlackDirectMessage;
import jp.co.highjunk.equipment.common.slack.SlackSendResult;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * A-07 のDM（④・③）が送れなかったとき（詳細設計 auth/A-07_自分のパスワード変更.md の11章、画面設計 9章の注意3）のテスト。
 * 送れなくても、パスワードの変更・ロックはそのままで、画面の返し方は変わらない。
 */
@DisplayName("A-07 自分のパスワード変更：DMが送れなかったとき")
class AuthUpdatePasswordSlackFailureApiTest extends AuthApiTestBase {

	private static final String NEW_PASSWORD = "Xq7#kd9!Lm2";

	/** テストの設定では、送り手は FakeSlackSender（テスト用の偽物の Bean）。送れなかった結果を返す偽物に置き換える。 */
	@MockitoBean
	FakeSlackSender slackSender;

	private org.springframework.test.web.servlet.ResultActions update(MockHttpSession session, String current,
			String password) throws Exception {
		return mockMvc.perform(put("/api/me/password")
				.session(session)
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"currentPassword\":\"" + current + "\",\"password\":\"" + password
						+ "\",\"passwordConfirm\":\"" + password + "\"}"));
	}

	private void changeWith(SlackSendResult result) throws Exception {
		when(slackSender.sendDirectMessage(eq(IPPAN), any(SlackDirectMessage.class))).thenReturn(result);
		MockHttpSession session = loginSession(IPPAN);

		update(session, PASSWORD, NEW_PASSWORD)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"));

		verify(slackSender, times(1)).sendDirectMessage(eq(IPPAN), any(SlackDirectMessage.class));
		login(IPPAN, NEW_PASSWORD).andExpect(status().isOk());
	}

	private void lockWith(SlackSendResult result) throws Exception {
		when(slackSender.sendDirectMessage(eq(IPPAN), any(SlackDirectMessage.class))).thenReturn(result);
		MockHttpSession session = loginSession(IPPAN);
		for (int i = 0; i < 4; i++) {
			update(session, "wrong-password", NEW_PASSWORD).andExpect(status().isBadRequest());
		}

		update(session, "wrong-password", NEW_PASSWORD)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].message").value("現在のパスワードが違います"));

		assertThat(jdbc.queryForObject("SELECT locked_at FROM users WHERE email = ?", Object.class, IPPAN)).isNotNull();
		verify(slackSender, times(1)).sendDirectMessage(eq(IPPAN), any(SlackDirectMessage.class));
		me(session).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("変更のDM（④）：Slack のユーザーが見つからない（USER_NOT_FOUND）でも、パスワードは変わる。同じ 200")
	void changedUserNotFound() throws Exception {
		changeWith(SlackSendResult.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("変更のDM（④）：そのほかの失敗（FAILED）でも、パスワードは変わる。同じ 200")
	void changedFailed() throws Exception {
		changeWith(SlackSendResult.FAILED);
	}

	@Test
	@DisplayName("ロックのDM（③）：送れなくても（USER_NOT_FOUND）、ロックはそのまま。この端末はログアウト。同じ 400")
	void lockUserNotFound() throws Exception {
		lockWith(SlackSendResult.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("ロックのDM（③）：送れなくても（FAILED）、ロックはそのまま。この端末はログアウト。同じ 400")
	void lockFailed() throws Exception {
		lockWith(SlackSendResult.FAILED);
	}
}
