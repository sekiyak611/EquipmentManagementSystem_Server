package jp.co.highjunk.equipment.dao.entity.custom;

import lombok.Getter;
import lombok.Setter;

/**
 * 種別ごとの備品の数（EquipmentCustomMapper#countGroupByCategoryId で数えた結果の1行）。
 * テーブルの1行ではないので、MyBatis Generator は作らない。手で書く Mapper の結果の形は dao.entity.custom に置く（CLAUDE.md 4.1）。
 */
@Getter
@Setter
public class CategoryEquipmentCount {

	/** 種別のID（equipment.category_id） */
	private Long categoryId;

	/** 削除されていない備品の数（管理状態は問わない） */
	private Long equipmentCount;
}
