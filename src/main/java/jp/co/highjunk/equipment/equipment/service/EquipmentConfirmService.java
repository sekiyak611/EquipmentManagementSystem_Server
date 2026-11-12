package jp.co.highjunk.equipment.equipment.service;

import jp.co.highjunk.equipment.dto.equipment.EquipmentListItem;

/**
 * C-06 備品を「確認した」にする（詳細設計 equipment/C-06_確認した.md）。中身は EquipmentConfirmServiceImpl。
 */
public interface EquipmentConfirmService {

	/** 最終確認日を今日にし、変えたあとの備品を C-01 の1件と同じ形で返す。 */
	EquipmentListItem confirm(long equipmentId, long userId);
}
