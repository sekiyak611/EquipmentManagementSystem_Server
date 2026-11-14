package jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem.service;

import jp.co.highjunk.equipment.common.validation.EquipmentExists;
import jp.co.highjunk.equipment.common.validation.EquipmentManaged;
import jp.co.highjunk.equipment.common.validation.ServiceCheckOrder;
import jp.co.highjunk.equipment.common.validation.ServiceErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * C-06 で Service に渡す値と、DB との整合性のチェック（詳細設計 equipment/management/C-06_確認した.md の2.5・6章）。
 * Controller が、パスの備品のID とログインしている人のユーザIDから作る（CLAUDE.md 5章）。
 * 確かめる順番：備品がある（404。備品の行を FOR UPDATE で押さえる）→ 管理中（400）。
 */
@Getter
@AllArgsConstructor
public class EquipmentManagementUpdateConfirmationItemServiceInDto {

	/** 備品のID（パスの値） */
	@EquipmentExists(forUpdate = true, groups = ServiceCheckOrder.First.class, payload = ServiceErrorType.NotFound.class)
	@EquipmentManaged(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.BusinessRule.class)
	private final Long id;

	/** ログインしている人のユーザID（変えた人に入れる） */
	private final long loginUserId;
}
