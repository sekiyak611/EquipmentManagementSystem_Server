package jp.co.highjunk.equipment.common.slack;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 偽物（FakeSlackSender）が記録した、送ったDMの1件（詳細設計 common.md 3.13）。
 */
@Getter
@AllArgsConstructor
public class SentSlackMessage {

	/** 宛先のメールアドレス。 */
	private final String email;

	/** 見出し。 */
	private final String title;

	/** 本文。 */
	private final String body;

	/** 送った日時（日本時間）。 */
	private final OffsetDateTime sentAt;
}
