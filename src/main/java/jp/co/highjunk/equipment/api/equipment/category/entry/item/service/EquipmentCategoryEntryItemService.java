package jp.co.highjunk.equipment.api.equipment.category.entry.item.service;

import jp.co.highjunk.equipment.api.equipment.category.entry.item.model.EquipmentCategoryEntryItemRestOutDto;
import jp.co.highjunk.equipment.common.error.FieldErrorException;

/**
 * K-02 種別を追加するの業務の処理（中身は EquipmentCategoryEntryItemServiceImpl。詳細設計 equipment/category/K-02_種別を追加する.md）。
 */
public interface EquipmentCategoryEntryItemService {

	/**
	 * 種別を1つ足し、作った種別を返す（K-01 の1件と同じ形）。
	 *
	 * @throws FieldErrorException 名前・頭文字が、ほかの種別と重なるとき（400）
	 */
	EquipmentCategoryEntryItemRestOutDto entry(EquipmentCategoryEntryItemServiceInDto serviceInDto);
}
