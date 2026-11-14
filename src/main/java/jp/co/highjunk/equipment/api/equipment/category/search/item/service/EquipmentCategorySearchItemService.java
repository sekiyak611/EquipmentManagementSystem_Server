package jp.co.highjunk.equipment.api.equipment.category.search.item.service;

import java.util.List;

import jp.co.highjunk.equipment.api.equipment.category.search.item.model.EquipmentCategorySearchItemRestOutDto;

/**
 * K-01 種別の一覧の業務の処理（中身は EquipmentCategorySearchItemServiceImpl。詳細設計 equipment/category/K-01_種別一覧.md）。
 */
public interface EquipmentCategorySearchItemService {

	/**
	 * 削除されていない種別を、使っている備品の数つきで返す。
	 * 並び順は、頭文字の順。頭文字がないものは後ろに名前の順（画面設計 16章）。0件なら空の一覧。
	 */
	List<EquipmentCategorySearchItemRestOutDto> search();
}
