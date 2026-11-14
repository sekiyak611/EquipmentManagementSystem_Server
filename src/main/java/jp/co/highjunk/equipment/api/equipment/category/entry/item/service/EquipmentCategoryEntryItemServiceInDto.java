package jp.co.highjunk.equipment.api.equipment.category.entry.item.service;

import jp.co.highjunk.equipment.common.validation.CategoryInitialNotDuplicated;
import jp.co.highjunk.equipment.common.validation.CategoryNameNotDuplicated;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * K-02 で Service に渡す値と、DB との整合性のチェック（詳細設計 equipment/category/K-02_種別を追加する.md の2.5・6章）。
 * Controller が、RestInDto とログインしている人のユーザIDから作る（CLAUDE.md 5章）。
 */
@Getter
@AllArgsConstructor
@CategoryNameNotDuplicated
@CategoryInitialNotDuplicated
public class EquipmentCategoryEntryItemServiceInDto
		implements CategoryNameNotDuplicated.Target, CategoryInitialNotDuplicated.Target {

	/** 名前（前後の空白を取り除いたもの） */
	private final String name;

	/** 頭文字（大文字に直したもの）。なければ null */
	private final String initial;

	/** ログインしている人のユーザID（作った人・変えた人に入れる） */
	private final long loginUserId;

	/** 足すときは、重なりを数えるときに除く種別がないので null（名前・頭文字のチェックが使う） */
	@Override
	public Long getId() {
		return null;
	}
}
