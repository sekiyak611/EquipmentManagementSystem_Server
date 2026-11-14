package jp.co.highjunk.equipment.common.text;

import java.util.Locale;

/**
 * 画面から受け取った文字の直し方（詳細設計 common.md）。RestInDto が、形式のチェックの前に使う。
 * 2つ以上の API で同じ直し方をするので、common に置く（CLAUDE.md 4.1。2026-11-15 に決めた）。
 * 例：種別の名前・頭文字（K-02・K-03。画面設計 16章）。
 */
public final class TextInputs {

	private TextInputs() {
	}

	/** 前後の空白（全角の空白も）を取り除く。null は null のまま。 */
	public static String strip(String value) {
		return value == null ? null : value.strip();
	}

	/**
	 * 前後の空白を取り除き、空なら null にする。そうでなければ大文字にする。
	 * 半角の a〜z だけが A〜Z になる（全角の英字は大文字になっても、形式のチェックで誤りになる）。
	 */
	public static String stripToUpperCaseOrNull(String value) {
		if (value == null) {
			return null;
		}
		String stripped = value.strip();
		return stripped.isEmpty() ? null : stripped.toUpperCase(Locale.ROOT);
	}
}
