package jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.service;

import jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.model.EquipmentMineUpdateReturnItemRestOutDto;

/**
 * B-03 備品を返すの業務の処理（詳細設計 equipment/mine/B-03_備品を返す.md）。
 */
public interface EquipmentMineUpdateReturnItemService {

	/**
	 * ログインしている人が借りている備品を返し、返したあとの最新の「自分の備品」一覧を返す。
	 *
	 * @throws jp.co.highjunk.equipment.common.error.NotFoundException     貸出履歴がない・取り消されている（404）
	 * @throws jp.co.highjunk.equipment.common.error.ForbiddenException    借りたのが自分でない（403）
	 * @throws jp.co.highjunk.equipment.common.error.ConflictException     すでに返却されている（409）
	 * @throws jp.co.highjunk.equipment.common.error.BusinessRuleException その備品が管理中でない（400）
	 */
	EquipmentMineUpdateReturnItemRestOutDto returnItem(EquipmentMineUpdateReturnItemServiceInDto serviceInDto);
}
