package jp.co.highjunk.equipment.common.slack;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.users.UsersLookupByEmailResponse;

import jp.co.highjunk.equipment.common.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 本物の Slack にDMを送る（詳細設計 common.md 3.13）。app.test-support.enabled が false のときだけ動く。
 * 見出し・本文（リンクの合言葉が入る）・トークンは、記録（ログ）に出さない（API設計 7章）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.test-support", name = "enabled", havingValue = "false", matchIfMissing = true)
public class SlackApiSender implements SlackSender {

	/** メールアドレスの Slack のユーザーが見つからないときの、Slack の error の名前。 */
	private static final String USER_NOT_FOUND = "users_not_found";

	private final MethodsClient methodsClient;
	private final AppProperties appProperties;

	@Override
	public SlackSendResult sendDirectMessage(String email, SlackDirectMessage message) {
		String token = appProperties.slack().botToken();
		if (token == null || token.isBlank()) {
			log.warn("Slackのボットトークンが設定されていません。DMを送れませんでした。宛先：{}", email);
			return SlackSendResult.FAILED;
		}
		try {
			UsersLookupByEmailResponse lookup = methodsClient.usersLookupByEmail(r -> r.email(email));
			if (!lookup.isOk()) {
				if (USER_NOT_FOUND.equals(lookup.getError())) {
					log.warn("SlackのDMを送れませんでした。結果：{}、宛先：{}", SlackSendResult.USER_NOT_FOUND, email);
					return SlackSendResult.USER_NOT_FOUND;
				}
				return failed(email, lookup.getError());
			}
			String userId = lookup.getUser().getId();
			ChatPostMessageResponse posted = methodsClient.chatPostMessage(r -> r
					.channel(userId)
					.text(message.toText())
					// リンクのプレビューを出さない（画面設計 9章の注意1）
					.unfurlLinks(false)
					.unfurlMedia(false));
			if (!posted.isOk()) {
				return failed(email, posted.getError());
			}
			return SlackSendResult.SENT;
		} catch (IOException | SlackApiException e) {
			// 例外の文に、Slack の返した中身が入ることがあるので、種類だけ残す
			return failed(email, e.getClass().getSimpleName());
		}
	}

	private SlackSendResult failed(String email, String error) {
		log.warn("SlackのDMを送れませんでした。結果：{}、Slackのerror：{}、宛先：{}", SlackSendResult.FAILED, error, email);
		return SlackSendResult.FAILED;
	}
}
