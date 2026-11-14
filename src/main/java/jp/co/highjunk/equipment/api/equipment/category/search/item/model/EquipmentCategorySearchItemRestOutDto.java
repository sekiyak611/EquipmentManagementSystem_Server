package jp.co.highjunk.equipment.api.equipment.category.search.item.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * K-01 で返す種別の1件（詳細設計 equipment/category/K-01_種別一覧.md の3章）。data にこれの配列を入れる。
 */
@Getter
@AllArgsConstructor
public class EquipmentCategorySearchItemRestOutDto {

	/** 種別のID */
	private final long id;

	/** 名前 */
	private final String name;

	/** 頭文字（半角の英大文字1文字）。ない種別は null */
	private final String initial;

	/** 使っている備品の数（削除されていない備品。管理状態は問わない） */
	private final long equipmentCount;

	/** 種別を変える（K-03）ときに送り返す番号（API設計 2.7 の楽観ロック） */
	private final int version;
}
