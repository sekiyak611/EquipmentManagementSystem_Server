package jp.co.highjunk.equipment.common.slack;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 送った内容を読み出すAPI（GET /api/test-support/slack-messages）が返す data（詳細設計 common.md 3.13）。
 */
@Getter
@AllArgsConstructor
public class SentSlackMessagesResponse {

	/** 送った内容の一覧。古い順。 */
	private final List<SentSlackMessage> messages;
}
