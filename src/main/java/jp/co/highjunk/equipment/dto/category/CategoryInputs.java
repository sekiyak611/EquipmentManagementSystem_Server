package jp.co.highjunk.equipment.dto.category;

import java.util.Locale;

/**
 * 種別の名前・頭文字の直し方（詳細設計 category/K-02_種別を追加する.md の2.3）。
 * K-02（CategoryCreateRequest）と K-03（CategoryUpdateRequest）が、入力チェックの前に使う。
 */
public final class CategoryInputs {

	private CategoryInputs() {
	}

	/** 名前の前後の空白（全角の空白も）を取り除く。null は null のまま。 */
	public static String name(String name) {
		return name == null ? null : name.strip();
	}

	/**
	 * 頭文字の前後の空白を取り除き、空なら null（頭文字なし）にする。そうでなければ大文字にする。
	 * 半角の a〜z だけが A〜Z になる（全角の英字は大文字になっても、入力チェックで誤りになる）。
	 */
	public static String initial(String initial) {
		if (initial == null) {
			return null;
		}
		String stripped = initial.strip();
		return stripped.isEmpty() ? null : stripped.toUpperCase(Locale.ROOT);
	}
}
