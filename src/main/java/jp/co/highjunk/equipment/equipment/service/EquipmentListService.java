package jp.co.highjunk.equipment.equipment.service;

import jp.co.highjunk.equipment.dto.equipment.EquipmentListRequest;
import jp.co.highjunk.equipment.dto.equipment.EquipmentListResponse;

/**
 * C-01 備品を探す（詳細設計 equipment/C-01_備品を探す.md）。中身は EquipmentListServiceImpl。
 */
public interface EquipmentListService {

	/** 条件に合う備品の、指定したページを返す。 */
	EquipmentListResponse list(EquipmentListRequest request);
}
