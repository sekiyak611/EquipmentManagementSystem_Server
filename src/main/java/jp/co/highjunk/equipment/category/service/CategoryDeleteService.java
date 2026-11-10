package jp.co.highjunk.equipment.category.service;

import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.common.error.NotFoundException;

/**
 * K-04 種別を消すの業務のルール（中身は CategoryDeleteServiceImpl。詳細設計 category/K-04_種別を消す.md）。
 */
public interface CategoryDeleteService {

	/**
	 * 種別を論理削除する。
	 *
	 * @param id     種別のID
	 * @param userId ログインしている人のユーザID（変えた人に入れる）
	 * @throws NotFoundException     種別がない・削除済みのとき（404）
	 * @throws BusinessRuleException 使っている備品があるとき（400）
	 */
	void delete(long id, long userId);
}
