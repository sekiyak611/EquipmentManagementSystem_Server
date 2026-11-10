package jp.co.highjunk.equipment.common.constant;

/**
 * ログインしたあと最初に出す画面（docs/API設計.md 4.2 の homePage、機能一覧 F-01）。
 */
public enum HomePage {

	/** 備品一覧（S-07）。「備品一覧を見る」（EQUIPMENT_VIEW）を持つ人 */
	EQUIPMENT_LIST,

	/** 自分の備品（S-05）。「備品一覧を見る」を持たない人 */
	MY_EQUIPMENT
}
