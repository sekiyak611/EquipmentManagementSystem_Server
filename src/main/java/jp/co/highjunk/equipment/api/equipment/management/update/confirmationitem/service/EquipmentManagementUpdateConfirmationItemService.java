package jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem.service;

import jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem.model.EquipmentManagementUpdateConfirmationItemRestOutDto;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.common.error.NotFoundException;

/**
 * C-06 備品を「確認した」にするの業務の処理（中身は EquipmentManagementUpdateConfirmationItemServiceImpl。
 * 詳細設計 equipment/management/C-06_確認した.md）。
 */
public interface EquipmentManagementUpdateConfirmationItemService {

	/**
	 * 最終確認日を今日にし、変えたあとの備品を C-01 の1件と同じ形で返す。
	 *
	 * @throws NotFoundException     備品がない・削除済みのとき（404）
	 * @throws BusinessRuleException 管理中でないとき（400）
	 */
	EquipmentManagementUpdateConfirmationItemRestOutDto confirm(
			EquipmentManagementUpdateConfirmationItemServiceInDto serviceInDto);
}
