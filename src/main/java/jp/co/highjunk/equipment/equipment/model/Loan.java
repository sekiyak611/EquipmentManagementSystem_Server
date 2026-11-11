package jp.co.highjunk.equipment.equipment.model;

/**
 * テーブル loans の1行（C-01 で使う列だけ。データ設計 5.7）。
 */
public class Loan {

	/** 備品のID */
	private long equipmentId;

	/** 借用者のユーザID */
	private long userId;

	public long getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(long equipmentId) {
		this.equipmentId = equipmentId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
}
