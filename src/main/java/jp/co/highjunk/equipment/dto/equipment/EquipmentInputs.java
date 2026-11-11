package jp.co.highjunk.equipment.dto.equipment;

/**
 * 備品の文字の項目の直し方（詳細設計 equipment/C-03_備品を登録する.md の2.3。2026-11-11 に人が決めた）。
 * 前後の空白（全角の空白も）を取り除き、空なら null（入力なし）にする。C-03・C-04 の dto が、入力チェックの前に使う。
 */
public final class EquipmentInputs {

	private EquipmentInputs() {
	}

	public static String text(String value) {
		if (value == null) {
			return null;
		}
		String stripped = value.strip();
		return stripped.isEmpty() ? null : stripped;
	}
}
