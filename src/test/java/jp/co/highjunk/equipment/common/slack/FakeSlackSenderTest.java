package jp.co.highjunk.equipment.common.slack;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * テスト用の偽物（FakeSlackSender）の単体テスト（詳細設計 common.md 3.13）。時計を止めて確かめる。
 */
class FakeSlackSenderTest {

	private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

	private static FakeSlackSender senderAt(int hour) {
		ZonedDateTime now = ZonedDateTime.of(2026, 11, 15, hour, 0, 0, 0, TOKYO);
		return new FakeSlackSender(Clock.fixed(now.toInstant(), TOKYO));
	}

	@Test
	@DisplayName("送ると SENT が返り、記録に宛先・見出し・本文・送った日時（日本時間）が、送った順で入る")
	void recordsInOrder() {
		FakeSlackSender sender = senderAt(9);

		SlackSendResult first = sender.sendDirectMessage("a@example.com", new SlackDirectMessage("見出し1", "本文1"));
		SlackSendResult second = sender.sendDirectMessage("b@example.com", new SlackDirectMessage("見出し2", "本文2"));

		assertThat(first).isEqualTo(SlackSendResult.SENT);
		assertThat(second).isEqualTo(SlackSendResult.SENT);
		List<SentSlackMessage> all = sender.findAll();
		assertThat(all).hasSize(2);
		assertThat(all.get(0).getEmail()).isEqualTo("a@example.com");
		assertThat(all.get(0).getTitle()).isEqualTo("見出し1");
		assertThat(all.get(0).getBody()).isEqualTo("本文1");
		assertThat(all.get(0).getSentAt()).isEqualTo(OffsetDateTime.of(2026, 11, 15, 9, 0, 0, 0, ZoneOffset.ofHours(9)));
		assertThat(all.get(1).getEmail()).isEqualTo("b@example.com");
	}

	@Test
	@DisplayName("clear で空になる")
	void clearEmpties() {
		FakeSlackSender sender = senderAt(9);
		sender.sendDirectMessage("a@example.com", new SlackDirectMessage("見出し", "本文"));

		sender.clear();

		assertThat(sender.findAll()).isEmpty();
	}

	@Test
	@DisplayName("記録を読んだあとに送っても、読んだ一覧は変わらない")
	void returnedListIsASnapshot() {
		FakeSlackSender sender = senderAt(9);
		sender.sendDirectMessage("a@example.com", new SlackDirectMessage("見出し", "本文"));
		List<SentSlackMessage> snapshot = sender.findAll();

		sender.sendDirectMessage("b@example.com", new SlackDirectMessage("見出し", "本文"));

		assertThat(snapshot).hasSize(1);
		assertThat(sender.findAll()).hasSize(2);
	}

	@Test
	@DisplayName("Slack に送る文は、見出しを太字にして、空行をはさみ、本文を続けた形")
	void messageText() {
		assertThat(new SlackDirectMessage("見出し", "本文1\n本文2").toText()).isEqualTo("*見出し*\n\n本文1\n本文2");
	}
}
