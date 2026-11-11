package jp.co.highjunk.equipment.equipment.model;

import java.time.LocalDate;

import jp.co.highjunk.equipment.common.constant.EquipmentStatus;

/**
 * テーブル equipment の1行（C-01 で使う列だけ。データ設計 5.6）。
 */
public class Equipment {

	/** ID */
	private long id;

	/** 管理番号 */
	private String managementNo;

	/** 種別のID */
	private long categoryId;

	/** メーカー */
	private String maker;

	/** モデル */
	private String model;

	/** 管理状態 */
	private EquipmentStatus status;

	/** 最終確認日（未記入なら null） */
	private LocalDate lastConfirmedOn;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getManagementNo() {
		return managementNo;
	}

	public void setManagementNo(String managementNo) {
		this.managementNo = managementNo;
	}

	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public String getMaker() {
		return maker;
	}

	public void setMaker(String maker) {
		this.maker = maker;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public EquipmentStatus getStatus() {
		return status;
	}

	public void setStatus(EquipmentStatus status) {
		this.status = status;
	}

	public LocalDate getLastConfirmedOn() {
		return lastConfirmedOn;
	}

	public void setLastConfirmedOn(LocalDate lastConfirmedOn) {
		this.lastConfirmedOn = lastConfirmedOn;
	}
}
