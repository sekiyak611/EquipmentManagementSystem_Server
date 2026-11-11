package jp.co.highjunk.equipment.equipment.service;

import jp.co.highjunk.equipment.dto.equipment.EquipmentCreateRequest;
import jp.co.highjunk.equipment.dto.equipment.EquipmentListItem;

/**
 * C-03 備品を登録する（詳細設計 equipment/C-03_備品を登録する.md）。中身は EquipmentCreateServiceImpl。
 */
public interface EquipmentCreateService {

	/** 備品を登録し、作った備品を C-01 の1件と同じ形で返す。 */
	EquipmentListItem create(EquipmentCreateRequest request, long userId);
}
