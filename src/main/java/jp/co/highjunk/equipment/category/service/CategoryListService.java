package jp.co.highjunk.equipment.category.service;

import java.util.List;

import jp.co.highjunk.equipment.dto.category.CategoryResponse;

/**
 * K-01 種別の一覧の業務のルール（中身は CategoryListServiceImpl。詳細設計 category/K-01_種別一覧.md）。
 */
public interface CategoryListService {

	/**
	 * 削除されていない種別を、使っている備品の数つきで返す。
	 * 並び順は、頭文字の順。頭文字がないものは後ろに名前の順（画面設計 16章）。0件なら空の一覧。
	 */
	List<CategoryResponse> list();
}
