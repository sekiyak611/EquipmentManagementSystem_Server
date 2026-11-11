package jp.co.highjunk.equipment.equipment.model;

/**
 * テーブル users の1行（C-01 で使う列だけ。データ設計 5.1）。
 */
public class User {

	/** ID */
	private long id;

	/** 氏名 */
	private String name;

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
}
