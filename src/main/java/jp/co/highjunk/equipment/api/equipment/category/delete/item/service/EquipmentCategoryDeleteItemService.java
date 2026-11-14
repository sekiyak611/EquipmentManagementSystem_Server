package jp.co.highjunk.equipment.api.equipment.category.delete.item.service;

import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.common.error.NotFoundException;

/**
 * K-04 種別を消すの業務の処理（中身は EquipmentCategoryDeleteItemServiceImpl。詳細設計 equipment/category/K-04_種別を消す.md）。
 */
public interface EquipmentCategoryDeleteItemService {

	/**
	 * 種別を論理削除する。
	 *
	 * @throws NotFoundException     種別がない・削除済みのとき（404）
	 * @throws BusinessRuleException 使っている備品があるとき（400）
	 */
	void delete(EquipmentCategoryDeleteItemServiceInDto serviceInDto);
}
