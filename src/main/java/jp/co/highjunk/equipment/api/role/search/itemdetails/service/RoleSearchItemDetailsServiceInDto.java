package jp.co.highjunk.equipment.api.role.search.itemdetails.service;

import jp.co.highjunk.equipment.common.validation.RoleExists;
import jp.co.highjunk.equipment.common.validation.ServiceCheckOrder;
import jp.co.highjunk.equipment.common.validation.ServiceErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * H-02 で Service に渡す値と、DB との整合性のチェック（詳細設計 role/H-02_権限1つを返す.md の2.5・6章）。
 * Controller が、パスの権限のID から作る（CLAUDE.md 5章）。読むだけなので、ログインしている人のユーザID は入れない。
 */
@Getter
@AllArgsConstructor
public class RoleSearchItemDetailsServiceInDto {

	/** 権限のID（パスの値） */
	@RoleExists(groups = ServiceCheckOrder.First.class, payload = ServiceErrorType.NotFound.class)
	private final Long id;
}
