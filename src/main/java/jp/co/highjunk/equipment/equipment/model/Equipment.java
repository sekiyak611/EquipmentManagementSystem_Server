package jp.co.highjunk.equipment.equipment.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jp.co.highjunk.equipment.common.constant.EquipmentStatus;

/**
 * テーブル equipment の1行（使う列だけ。データ設計 5.6）。
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

	/** シリアルNo */
	private String serialNo;

	/** サイズ */
	private String size;

	/** 解像度 */
	private String resolution;

	/** 昇降（true＝あり、false＝なし、null＝未設定） */
	private Boolean heightAdjustable;

	/** ピボット（同上） */
	private Boolean pivot;

	/** 購入日 */
	private LocalDate purchasedOn;

	/** 保証期日 */
	private LocalDate warrantyUntil;

	/** 備考 */
	private String note;

	/** 楽観ロックの番号（データ設計 2章） */
	private int version;

	/** 作った日時 */
	private LocalDateTime createdAt;

	/** 作った人のユーザID */
	private Long createdBy;

	/** 最後に変えた日時 */
	private LocalDateTime updatedAt;

	/** 最後に変えた人のユーザID */
	private Long updatedBy;

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

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public Boolean getHeightAdjustable() {
		return heightAdjustable;
	}

	public void setHeightAdjustable(Boolean heightAdjustable) {
		this.heightAdjustable = heightAdjustable;
	}

	public Boolean getPivot() {
		return pivot;
	}

	public void setPivot(Boolean pivot) {
		this.pivot = pivot;
	}

	public LocalDate getPurchasedOn() {
		return purchasedOn;
	}

	public void setPurchasedOn(LocalDate purchasedOn) {
		this.purchasedOn = purchasedOn;
	}

	public LocalDate getWarrantyUntil() {
		return warrantyUntil;
	}

	public void setWarrantyUntil(LocalDate warrantyUntil) {
		this.warrantyUntil = warrantyUntil;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}
}
