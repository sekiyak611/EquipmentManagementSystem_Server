package jp.co.highjunk.equipment.common.slack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 1通のSlackのDM（詳細設計 common.md 3.13）。文面そのもの（画面設計 9章の①〜⑦）は、送る機能が作って渡す。
 */
@Getter
@RequiredArgsConstructor
public class SlackDirectMessage {

	/** 見出し。 */
	private final String title;

	/** 本文。 */
	private final String body;

	/** Slack に送る文。見出しを太字にして、空行をはさみ、本文を続ける。 */
	public String toText() {
		return "*" + title + "*\n\n" + body;
	}
}
