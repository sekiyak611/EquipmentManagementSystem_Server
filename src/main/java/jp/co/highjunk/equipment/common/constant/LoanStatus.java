package jp.co.highjunk.equipment.common.constant;

/**
 * 備品の貸出状態（C-01 の返す値。詳細設計 equipment/management/C-01_備品を探す.md 3章）。貸出中＝返却日が空で、取り消されていない貸出履歴がある（データ設計 5.7）。
 */
public enum LoanStatus {

	/** 保管中（貸出中でない） */
	IN_STOCK,

	/** 貸出中 */
	LENT
}
