package jp.co.highjunk.equipment.category.model;

/**
 * 種別ごとの備品の数（EquipmentMapper#countGroupByCategoryId で数えた結果の1行）。
 * テーブルの1行ではないが、mapper が使ってよいのは同じ機能の model と common だけなので、ここに置く（詳細設計 category/category.md 4章の No.1）。
 */
public class CategoryEquipmentCount {

	/** 種別のID（equipment.category_id） */
	private long categoryId;

	/** 削除されていない備品の数 */
	private long equipmentCount;

	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public long getEquipmentCount() {
		return equipmentCount;
	}

	public void setEquipmentCount(long equipmentCount) {
		this.equipmentCount = equipmentCount;
	}
}
