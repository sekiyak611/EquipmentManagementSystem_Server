package jp.co.highjunk.equipment.common.constant;

/**
 * 備品の管理状態（docs/データ設計.md 5.6 の equipment.status と同じ文字）。
 */
public enum EquipmentStatus {

	/** 管理中 */
	MANAGED,

	/** 廃棄 */
	DISPOSED,

	/** 不明 */
	MISSING
}
