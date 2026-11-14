package jp.co.highjunk.equipment.api.equipment.management.entry.item.service;

import jp.co.highjunk.equipment.api.equipment.management.entry.item.model.EquipmentManagementEntryItemRestOutDto;
import jp.co.highjunk.equipment.common.error.FieldErrorException;

/**
 * C-03 備品を登録するの業務の処理（中身は EquipmentManagementEntryItemServiceImpl。詳細設計 equipment/management/C-03_備品を登録する.md）。
 */
public interface EquipmentManagementEntryItemService {

	/**
	 * 備品を登録し、作った備品を C-01 の1件と同じ形で返す。
	 *
	 * @throws FieldErrorException 管理番号が重なる・種別がない（ServiceInDto）、購入日・保証期日の決まりに合わない（Validator）とき（400）
	 */
	EquipmentManagementEntryItemRestOutDto entry(EquipmentManagementEntryItemServiceInDto serviceInDto);
}
