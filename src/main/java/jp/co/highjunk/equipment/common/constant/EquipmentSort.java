package jp.co.highjunk.equipment.common.constant;

/**
 * 備品の並び順の列（C-01 の条件 sort。API設計 4.8）。
 */
public enum EquipmentSort {

	/** 管理番号（sort=managementNo） */
	MANAGEMENT_NO,

	/** 最終確認日（sort=lastConfirmedOn） */
	LAST_CONFIRMED_ON
}
