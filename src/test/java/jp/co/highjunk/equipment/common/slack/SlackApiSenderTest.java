package jp.co.highjunk.equipment.common.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.util.unit.DataSize;

import com.slack.api.methods.MethodsClient;
import com.slack.api.RequestConfigurator;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.users.UsersLookupByEmailRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.users.UsersLookupByEmailResponse;
import com.slack.api.model.User;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jp.co.highjunk.equipment.common.config.AppProperties;

/**
 * 本物の Slack に送る部品（SlackApiSender）の単体テスト（詳細設計 common.md 3.13）。
 * Slack には実際につながず、Slack Java SDK の部品（MethodsClient）を Mockito の偽物にして確かめる。
 */
class SlackApiSenderTest {

	private static final String TOKEN = "xoxb-secret-token";
	private static final String EMAIL = "taro@example.com";
	private static final SlackDirectMessage MESSAGE = new SlackDirectMessage("【備品管理】パスワード再設定のご案内",
			"リンク：http://localhost:8080/password/set?token=SECRET-LINK-TOKEN");

	private final MethodsClient client = mock(MethodsClient.class);
	private final Logger senderLogger = (Logger) LoggerFactory.getLogger(SlackApiSender.class);
	private final ListAppender<ILoggingEvent> logs = new ListAppender<>();

	@BeforeEach
	void attachLogAppender() {
		logs.start();
		senderLogger.addAppender(logs);
	}

	@AfterEach
	void detachLogAppender() {
		senderLogger.detachAppender(logs);
	}

	private static AppProperties props(String token) {
		return new AppProperties("http://localhost:8080", ZoneId.of("Asia/Tokyo"), "0 0 9 * * *", Duration.ofDays(60),
				new AppProperties.PasswordLink(Duration.ofMinutes(30), Duration.ofMinutes(30), Duration.ofDays(3),
						Duration.ofMinutes(5)),
				5, DataSize.ofMegabytes(1), new AppProperties.Slack(token), null, new AppProperties.TestSupport(false));
	}

	private SlackApiSender sender(String token) {
		return new SlackApiSender(client, props(token));
	}

	private static UsersLookupByEmailResponse found(String userId) {
		User user = new User();
		user.setId(userId);
		UsersLookupByEmailResponse response = new UsersLookupByEmailResponse();
		response.setOk(true);
		response.setUser(user);
		return response;
	}

	private static UsersLookupByEmailResponse lookupError(String error) {
		UsersLookupByEmailResponse response = new UsersLookupByEmailResponse();
		response.setOk(false);
		response.setError(error);
		return response;
	}

	private static ChatPostMessageResponse posted(boolean ok, String error) {
		ChatPostMessageResponse response = new ChatPostMessageResponse();
		response.setOk(ok);
		response.setError(error);
		return response;
	}

	private void lookupReturns(UsersLookupByEmailResponse response) throws Exception {
		when(client.usersLookupByEmail(anyLookup())).thenReturn(response);
	}

	private void postReturns(ChatPostMessageResponse response) throws Exception {
		when(client.chatPostMessage(anyPost())).thenReturn(response);
	}

	private static RequestConfigurator<UsersLookupByEmailRequest.UsersLookupByEmailRequestBuilder> anyLookup() {
		return any();
	}

	private static RequestConfigurator<ChatPostMessageRequest.ChatPostMessageRequestBuilder> anyPost() {
		return any();
	}

	@Test
	@DisplayName("送れた：SENT。見つけたユーザーのIDを宛先に、見出しを太字にした文を、リンクのプレビューなしで送る")
	void sendsToFoundUserWithoutUnfurl() throws Exception {
		lookupReturns(found("U12345"));
		postReturns(posted(true, null));

		SlackSendResult result = sender(TOKEN).sendDirectMessage(EMAIL, MESSAGE);

		assertThat(result).isEqualTo(SlackSendResult.SENT);
		ArgumentCaptor<RequestConfigurator<UsersLookupByEmailRequest.UsersLookupByEmailRequestBuilder>> lookupCaptor = ArgumentCaptor
				.forClass(RequestConfigurator.class);
		verify(client).usersLookupByEmail(lookupCaptor.capture());
		assertThat(lookupCaptor.getValue().configure(UsersLookupByEmailRequest.builder()).build().getEmail())
				.isEqualTo(EMAIL);
		ArgumentCaptor<RequestConfigurator<ChatPostMessageRequest.ChatPostMessageRequestBuilder>> postCaptor = ArgumentCaptor
				.forClass(RequestConfigurator.class);
		verify(client).chatPostMessage(postCaptor.capture());
		ChatPostMessageRequest request = postCaptor.getValue().configure(ChatPostMessageRequest.builder()).build();
		assertThat(request.getChannel()).isEqualTo("U12345");
		assertThat(request.getText()).isEqualTo("*【備品管理】パスワード再設定のご案内*\n\n" + MESSAGE.getBody());
		assertThat(request.isUnfurlLinks()).isFalse();
		assertThat(request.isUnfurlMedia()).isFalse();
	}

	@Test
	@DisplayName("Slack のユーザーが見つからない（users_not_found）：USER_NOT_FOUND で、メッセージは送らない")
	void userNotFound() throws Exception {
		lookupReturns(lookupError("users_not_found"));

		SlackSendResult result = sender(TOKEN).sendDirectMessage(EMAIL, MESSAGE);

		assertThat(result).isEqualTo(SlackSendResult.USER_NOT_FOUND);
		verify(client, never()).chatPostMessage(anyPost());
	}

	@Test
	@DisplayName("ユーザーを探すとき、Slack がほかの error で断った：FAILED で、メッセージは送らない")
	void lookupRejected() throws Exception {
		lookupReturns(lookupError("missing_scope"));

		SlackSendResult result = sender(TOKEN).sendDirectMessage(EMAIL, MESSAGE);

		assertThat(result).isEqualTo(SlackSendResult.FAILED);
		verify(client, never()).chatPostMessage(anyPost());
	}

	@Test
	@DisplayName("メッセージを送るとき、Slack が ok を返さない：FAILED")
	void postRejected() throws Exception {
		lookupReturns(found("U12345"));
		postReturns(posted(false, "channel_not_found"));

		assertThat(sender(TOKEN).sendDirectMessage(EMAIL, MESSAGE)).isEqualTo(SlackSendResult.FAILED);
	}

	@Test
	@DisplayName("通信の失敗：FAILED（例外は投げない）")
	void communicationFailure() throws Exception {
		when(client.usersLookupByEmail(anyLookup())).thenThrow(new IOException("接続できません"));

		assertThat(sender(TOKEN).sendDirectMessage(EMAIL, MESSAGE)).isEqualTo(SlackSendResult.FAILED);
	}

	@Test
	@DisplayName("ボットトークンが空：FAILED で、Slack を呼ばない")
	void blankToken() {
		assertThat(sender("").sendDirectMessage(EMAIL, MESSAGE)).isEqualTo(SlackSendResult.FAILED);
		assertThat(sender(null).sendDirectMessage(EMAIL, MESSAGE)).isEqualTo(SlackSendResult.FAILED);

		verifyNoInteractions(client);
	}

	@Test
	@DisplayName("送れなかったとき、記録に宛先と結果は残るが、見出し・本文・トークンは出ない")
	void logsDoNotContainBodyOrToken() throws Exception {
		lookupReturns(found("U12345"));
		postReturns(posted(false, "channel_not_found"));
		sender(TOKEN).sendDirectMessage(EMAIL, MESSAGE);
		lookupReturns(lookupError("users_not_found"));
		sender(TOKEN).sendDirectMessage(EMAIL, MESSAGE);

		assertThat(logs.list).hasSize(2);
		for (ILoggingEvent event : logs.list) {
			String line = event.getFormattedMessage();
			assertThat(line).contains(EMAIL);
			assertThat(line).doesNotContain("SECRET-LINK-TOKEN").doesNotContain(MESSAGE.getTitle())
					.doesNotContain(TOKEN);
		}
		assertThat(logs.list.get(0).getFormattedMessage()).contains("FAILED").contains("channel_not_found");
		assertThat(logs.list.get(1).getFormattedMessage()).contains("USER_NOT_FOUND");
	}
}
