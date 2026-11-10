package jp.co.highjunk.equipment.category.service;

import jp.co.highjunk.equipment.common.error.ConflictException;
import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.common.error.NotFoundException;
import jp.co.highjunk.equipment.dto.category.CategoryResponse;
import jp.co.highjunk.equipment.dto.category.CategoryUpdateRequest;

/**
 * K-03 種別を変えるの業務のルール（中身は CategoryUpdateServiceImpl。詳細設計 category/K-03_種別を変える.md）。
 */
public interface CategoryUpdateService {

	/**
	 * 種別の名前・頭文字を変え、変えた種別を返す（K-01 の1件と同じ形）。
	 *
	 * @param id      種別のID
	 * @param request 受け取った値（空白と大文字は直したもの）
	 * @param userId  ログインしている人のユーザID（変えた人に入れる）
	 * @throws NotFoundException   種別がない・削除済みのとき（404）
	 * @throws ConflictException   ほかの人が先に保存していたとき（409）
	 * @throws FieldErrorException 名前・頭文字が、ほかの種別と重なるとき（400）
	 */
	CategoryResponse update(long id, CategoryUpdateRequest request, long userId);
}
