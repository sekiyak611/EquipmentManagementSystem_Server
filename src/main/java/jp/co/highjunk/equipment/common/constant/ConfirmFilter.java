package jp.co.highjunk.equipment.common.constant;

/**
 * 今月確認済みかの絞り込み（C-01 の条件 confirm。API設計 4.8）。管理中の備品にだけ効かせる（詳細設計 equipment/management/C-01_備品を探す.md 5章の No.5）。
 */
public enum ConfirmFilter {

	/** すべて */
	ALL,

	/** 今月確認済みだけ */
	CONFIRMED,

	/** 未確認だけ（最終確認日が未記入か先月以前） */
	UNCONFIRMED
}
