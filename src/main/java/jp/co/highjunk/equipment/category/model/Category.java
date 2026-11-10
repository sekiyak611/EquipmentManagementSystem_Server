package jp.co.highjunk.equipment.category.model;

/**
 * categories（種別）の1行。K-01 で使う列だけを持つ（データ設計 5.5）。
 */
public class Category {

	private long id;

	/** 名前 */
	private String name;

	/** 頭文字（半角の英大文字1文字）。ない種別は null */
	private String initial;

	/** 楽観ロックの番号（データ設計 2章） */
	private int version;

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
}
