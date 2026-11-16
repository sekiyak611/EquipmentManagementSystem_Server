package jp.co.highjunk.equipment.api.equipment.mine.search.item.service;

import jp.co.highjunk.equipment.api.equipment.mine.search.item.model.EquipmentMineSearchItemRestOutDto;

/**
 * B-01 自分が借りている備品の業務の処理（詳細設計 equipment/mine/B-01_自分の備品の一覧.md）。
 */
public interface EquipmentMineSearchItemService {

	/** ログインしている人が借りている備品の一覧と、今月の確認の案内を返す */
	EquipmentMineSearchItemRestOutDto search(EquipmentMineSearchItemServiceInDto serviceInDto);
}
