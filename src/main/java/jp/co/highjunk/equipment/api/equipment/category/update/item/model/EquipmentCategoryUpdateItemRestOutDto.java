package jp.co.highjunk.equipment.api.equipment.category.update.item.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * K-03 で返す、変えた種別（K-01 の1件と同じ形。詳細設計 equipment/category/K-03_種別を変える.md の3章）。
 */
@Getter
@AllArgsConstructor
public class EquipmentCategoryUpdateItemRestOutDto {

	/** 種別のID */
	private final long id;

	/** 名前 */
	private final String name;

	/** 頭文字。ない種別は null */
	private final String initial;

	/** 使っている備品の数（削除されていない備品。管理状態は問わない） */
	private final long equipmentCount;

	/** 変えたあとの version */
	private final int version;
}
