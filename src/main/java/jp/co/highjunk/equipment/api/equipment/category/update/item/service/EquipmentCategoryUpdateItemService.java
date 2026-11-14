package jp.co.highjunk.equipment.api.equipment.category.update.item.service;

import jp.co.highjunk.equipment.api.equipment.category.update.item.model.EquipmentCategoryUpdateItemRestOutDto;
import jp.co.highjunk.equipment.common.error.ConflictException;
import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.common.error.NotFoundException;

/**
 * K-03 種別を変えるの業務の処理（中身は EquipmentCategoryUpdateItemServiceImpl。詳細設計 equipment/category/K-03_種別を変える.md）。
 */
public interface EquipmentCategoryUpdateItemService {

	/**
	 * 種別の名前・頭文字を変え、変えた種別を返す（K-01 の1件と同じ形）。
	 *
	 * @throws NotFoundException   種別がない・削除済みのとき（404）
	 * @throws ConflictException   ほかの人が先に保存していたとき（409）
	 * @throws FieldErrorException 名前・頭文字が、ほかの種別と重なるとき（400）
	 */
	EquipmentCategoryUpdateItemRestOutDto update(EquipmentCategoryUpdateItemServiceInDto serviceInDto);
}
