package jp.co.highjunk.equipment.api.equipment.mine.update.item.service;

import jp.co.highjunk.equipment.api.equipment.mine.update.item.model.EquipmentMineUpdateItemRestOutDto;

/**
 * B-04 自分が借りている備品を「確認した」にするの業務の処理（詳細設計 equipment/mine/B-04_確認した.md）。
 */
public interface EquipmentMineUpdateItemService {

	/**
	 * ログインしている人が借りている備品の最終確認日を、まとめて今日にし、確認したあとの最新の「自分の備品」一覧を返す。
	 * 1つでも守れなければ、どれも更新しない。
	 *
	 * @throws jp.co.highjunk.equipment.common.error.NotFoundException     備品がない・削除済み（404）
	 * @throws jp.co.highjunk.equipment.common.error.ForbiddenException    自分が借りていない備品が含まれる（403）
	 * @throws jp.co.highjunk.equipment.common.error.BusinessRuleException 管理中でない備品が含まれる（400）
	 */
	EquipmentMineUpdateItemRestOutDto confirm(EquipmentMineUpdateItemServiceInDto serviceInDto);
}
