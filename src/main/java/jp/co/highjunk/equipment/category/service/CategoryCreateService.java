package jp.co.highjunk.equipment.category.service;

import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.dto.category.CategoryCreateRequest;
import jp.co.highjunk.equipment.dto.category.CategoryResponse;

/**
 * K-02 種別を追加するの業務のルール（中身は CategoryCreateServiceImpl。詳細設計 category/K-02_種別を追加する.md）。
 */
public interface CategoryCreateService {

	/**
	 * 種別を1つ足し、作った種別を返す（K-01 の1件と同じ形）。
	 *
	 * @param request 受け取った値（空白と大文字は直したもの）
	 * @param userId  ログインしている人のユーザID（作った人・変えた人に入れる）
	 * @throws FieldErrorException 名前・頭文字が、ほかの種別と重なるとき（400）
	 */
	CategoryResponse create(CategoryCreateRequest request, long userId);
}
