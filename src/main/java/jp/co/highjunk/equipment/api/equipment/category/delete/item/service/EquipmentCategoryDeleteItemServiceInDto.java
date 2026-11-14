package jp.co.highjunk.equipment.api.equipment.category.delete.item.service;

import jp.co.highjunk.equipment.common.validation.CategoryExists;
import jp.co.highjunk.equipment.common.validation.CategoryNotInUse;
import jp.co.highjunk.equipment.common.validation.ServiceCheckOrder;
import jp.co.highjunk.equipment.common.validation.ServiceErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * K-04 で Service に渡す値と、DB との整合性のチェック（詳細設計 equipment/category/K-04_種別を消す.md の2.5・6章）。
 * Controller が、パスの種別のID とログインしている人のユーザIDから作る（CLAUDE.md 5章）。
 * 確かめる順番：種別がある（404。種別の行を FOR UPDATE で押さえる）→ 使っている備品がない（400。備品を FOR SHARE で数える）。
 */
@Getter
@AllArgsConstructor
public class EquipmentCategoryDeleteItemServiceInDto {

	/** 種別のID（パスの値） */
	@CategoryExists(forUpdate = true, groups = ServiceCheckOrder.First.class, payload = ServiceErrorType.NotFound.class)
	@CategoryNotInUse(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.BusinessRule.class)
	private final Long id;

	/** ログインしている人のユーザID（変えた人に入れる） */
	private final long loginUserId;
}
