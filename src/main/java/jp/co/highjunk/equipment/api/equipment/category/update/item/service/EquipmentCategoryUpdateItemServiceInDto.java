package jp.co.highjunk.equipment.api.equipment.category.update.item.service;

import jp.co.highjunk.equipment.common.validation.CategoryExists;
import jp.co.highjunk.equipment.common.validation.CategoryInitialNotDuplicated;
import jp.co.highjunk.equipment.common.validation.CategoryNameNotDuplicated;
import jp.co.highjunk.equipment.common.validation.CategoryVersionMatches;
import jp.co.highjunk.equipment.common.validation.ServiceCheckOrder;
import jp.co.highjunk.equipment.common.validation.ServiceErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * K-03 で Service に渡す値と、DB との整合性のチェック（詳細設計 equipment/category/K-03_種別を変える.md の2.5・6章）。
 * Controller が、パスの種別のID・RestInDto・ログインしている人のユーザIDから作る（CLAUDE.md 5章）。
 * 確かめる順番：種別がある（404）→ version が同じ（409）→ 名前・頭文字が重ならない（400）。
 */
@Getter
@AllArgsConstructor
@CategoryVersionMatches(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.Conflict.class)
@CategoryNameNotDuplicated
@CategoryInitialNotDuplicated
public class EquipmentCategoryUpdateItemServiceInDto implements CategoryVersionMatches.Target,
		CategoryNameNotDuplicated.Target, CategoryInitialNotDuplicated.Target {

	/** 種別のID（パスの値） */
	@CategoryExists(groups = ServiceCheckOrder.First.class, payload = ServiceErrorType.NotFound.class)
	private final Long id;

	/** 名前（前後の空白を取り除いたもの） */
	private final String name;

	/** 頭文字（大文字に直したもの）。なければ null */
	private final String initial;

	/** 画面を開いたときに受け取った version */
	private final Integer version;

	/** ログインしている人のユーザID（変えた人に入れる） */
	private final long loginUserId;
}
