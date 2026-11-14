package jp.co.highjunk.equipment.api.equipment.management.search.item.service;

import jp.co.highjunk.equipment.api.equipment.management.search.item.model.EquipmentManagementSearchItemRestOutDto;

/**
 * C-01 備品を探すの業務の処理（中身は EquipmentManagementSearchItemServiceImpl。詳細設計 equipment/management/C-01_備品を探す.md）。
 */
public interface EquipmentManagementSearchItemService {

	/** 条件に合う備品の、指定したページを返す。 */
	EquipmentManagementSearchItemRestOutDto search(EquipmentManagementSearchItemServiceInDto serviceInDto);
}
