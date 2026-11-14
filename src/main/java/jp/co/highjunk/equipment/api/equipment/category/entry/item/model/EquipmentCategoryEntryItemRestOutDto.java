package jp.co.highjunk.equipment.api.equipment.category.entry.item.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * K-02 で返す、作った種別（K-01 の1件と同じ形。詳細設計 equipment/category/K-02_種別を追加する.md の3章）。
 */
@Getter
@AllArgsConstructor
public class EquipmentCategoryEntryItemRestOutDto {

	/** 種別のID */
	private final long id;

	/** 名前 */
	private final String name;

	/** 頭文字。ない種別は null */
	private final String initial;

	/** 使っている備品の数（作ったばかりなので 0） */
	private final long equipmentCount;

	/** version（作ったばかりなので 0） */
	private final int version;
}
