package jp.co.highjunk.equipment.api.auth.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jp.co.highjunk.equipment.common.slack.SlackDirectMessage;
import jp.co.highjunk.equipment.common.slack.SlackSendResult;
import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;

/**
 * A-01 のロックのDMが送れなかったとき（詳細設計 auth/A-01_ログイン.md 4.1、画面設計 9章の注意3）のテスト。
 * 送れなくても、ロックはそのままで、画面の文・401 は変わらない。
 */
@DisplayName("A-01 ログイン：ロックのDMが送れなかったとき")
class AuthLoginLockSlackFailureApiTest extends AuthApiTestBase {

	/** テストの設定では、送り手は FakeSlackSender（テスト用の偽物の Bean）。送れなかった結果を返す偽物に置き換える。 */
	@MockitoBean
	FakeSlackSender slackSender;

	private void lockWith(SlackSendResult result) throws Exception {
		when(slackSender.sendDirectMessage(eq(IPPAN), any(SlackDirectMessage.class))).thenReturn(result);
		for (int i = 0; i < 4; i++) {
			login(IPPAN, "wrong-password").andExpect(status().isUnauthorized());
		}
		login(IPPAN, "wrong-password")
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(LOGIN_FAILED));
	}

	private void assertLockedOnce() {
		assertThat(jdbc.queryForObject("SELECT locked_at FROM users WHERE email = ?", Object.class, IPPAN)).isNotNull();
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM password_tokens WHERE user_id = ? AND purpose = 'LOCK'",
				Integer.class, userId(IPPAN))).isEqualTo(1);
		verify(slackSender, times(1)).sendDirectMessage(eq(IPPAN), any(SlackDirectMessage.class));
	}

	@Test
	@DisplayName("Slack のユーザーが見つからない（USER_NOT_FOUND）：ロックはそのまま。401・同じ文")
	void userNotFound() throws Exception {
		lockWith(SlackSendResult.USER_NOT_FOUND);
		assertLockedOnce();
	}

	@Test
	@DisplayName("そのほかの失敗（FAILED）：ロックはそのまま。401・同じ文")
	void failed() throws Exception {
		lockWith(SlackSendResult.FAILED);
		assertLockedOnce();
	}
}
