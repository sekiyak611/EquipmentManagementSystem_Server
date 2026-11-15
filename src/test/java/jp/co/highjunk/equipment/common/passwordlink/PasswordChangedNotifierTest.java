package jp.co.highjunk.equipment.common.passwordlink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.common.slack.SentSlackMessage;
import jp.co.highjunk.equipment.common.slack.SlackDirectMessage;
import jp.co.highjunk.equipment.common.slack.SlackSendResult;
import jp.co.highjunk.equipment.common.slack.SlackSender;

/**
 * パスワードが変わったことのDM（④。詳細設計 common.md 3.15）の単体テスト。
 */
class PasswordChangedNotifierTest {

	private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

	@Test
	@DisplayName("本人に、見出し「【備品管理】パスワードを変更しました」と、変えた日時・心当たりがないときのことを送る")
	void sendsMessage() {
		FakeSlackSender slack = new FakeSlackSender(
				Clock.fixed(ZonedDateTime.of(2026, 11, 15, 21, 5, 0, 0, TOKYO).toInstant(), TOKYO));
		PasswordChangedNotifier notifier = new PasswordChangedNotifier(slack);

		notifier.notifyChanged("yamada@example.com", 7L, LocalDateTime.of(2026, 11, 15, 21, 3, 9));

		assertThat(slack.findAll()).hasSize(1);
		SentSlackMessage dm = slack.findAll().get(0);
		assertThat(dm.getEmail()).isEqualTo("yamada@example.com");
		assertThat(dm.getTitle()).isEqualTo("【備品管理】パスワードを変更しました");
		assertThat(dm.getBody())
				.contains("変更した日時：2026年11月15日 21:03")
				.contains("覚えがない場合は、すぐにパスワードを再設定し、総務に連絡してください");
	}

	@Test
	@DisplayName("日時は、月・日・時が1けたでも、0を付けない（2026年1月5日 9:07）")
	void dateFormat() {
		FakeSlackSender slack = new FakeSlackSender(Clock.system(TOKYO));
		new PasswordChangedNotifier(slack).notifyChanged("a@example.com", 1L, LocalDateTime.of(2026, 1, 5, 9, 7, 0));

		assertThat(slack.findAll().get(0).getBody()).contains("変更した日時：2026年1月5日 9:07");
	}

	@Test
	@DisplayName("送れなくても（USER_NOT_FOUND・FAILED）、例外にしない")
	void doesNotThrowWhenFailed() {
		SlackSender slack = mock(SlackSender.class);
		when(slack.sendDirectMessage(eq("a@example.com"), any(SlackDirectMessage.class)))
				.thenReturn(SlackSendResult.USER_NOT_FOUND, SlackSendResult.FAILED);
		PasswordChangedNotifier notifier = new PasswordChangedNotifier(slack);

		assertThatCode(() -> notifier.notifyChanged("a@example.com", 1L, LocalDateTime.of(2026, 1, 5, 9, 7, 0)))
				.doesNotThrowAnyException();
		assertThatCode(() -> notifier.notifyChanged("a@example.com", 1L, LocalDateTime.of(2026, 1, 5, 9, 7, 0)))
				.doesNotThrowAnyException();
	}
}
