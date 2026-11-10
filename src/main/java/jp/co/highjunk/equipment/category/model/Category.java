package jp.co.highjunk.equipment.category.model;

import java.time.LocalDateTime;

/**
 * categories（種別）の1行。K-01・K-02 で使う列だけを持つ（データ設計 5.5・2章）。
 */
public class Category {

	private long id;

	/** 名前 */
	private String name;

	/** 頭文字（半角の英大文字1文字）。ない種別は null */
	private String initial;

	/** 楽観ロックの番号（データ設計 2章） */
	private int version;

	/** 作った日時（日本時間） */
	private LocalDateTime createdAt;

	/** 作った人のユーザID */
	private Long createdBy;

	/** 最後に変えた日時（日本時間） */
	private LocalDateTime updatedAt;

	/** 最後に変えた人のユーザID */
	private Long updatedBy;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInitial() {
		return initial;
	}

	public void setInitial(String initial) {
		this.initial = initial;
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
