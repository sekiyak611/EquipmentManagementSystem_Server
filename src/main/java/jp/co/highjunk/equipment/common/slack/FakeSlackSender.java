package jp.co.highjunk.equipment.common.slack;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * テスト用の偽物（詳細設計 common.md 3.13）。Slack には送らず、送った内容を記録にためる。結果は、いつも SENT。
 * app.test-support.enabled が true のときだけ動く（本番・開発では動かない。CLAUDE.md 5章）。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.test-support", name = "enabled", havingValue = "true")
public class FakeSlackSender implements SlackSender {

	private final Clock clock;

	/** 送った順の記録。メモリの中だけに持つ。 */
	private final List<SentSlackMessage> sent = new CopyOnWriteArrayList<>();

	@Override
	public SlackSendResult sendDirectMessage(String email, SlackDirectMessage message) {
		sent.add(new SentSlackMessage(email, message.getTitle(), message.getBody(), OffsetDateTime.now(clock)));
		return SlackSendResult.SENT;
	}

	/** 送った内容の一覧（古い順）。読んだあとに送っても、返した一覧は変わらない。 */
	public List<SentSlackMessage> findAll() {
		return List.copyOf(sent);
	}

	/** 記録を空にする。 */
	public void clear() {
		sent.clear();
	}
}
