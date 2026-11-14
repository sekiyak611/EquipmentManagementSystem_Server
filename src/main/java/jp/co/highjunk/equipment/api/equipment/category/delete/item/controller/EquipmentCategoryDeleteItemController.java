package jp.co.highjunk.equipment.api.equipment.category.delete.item.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.api.equipment.category.delete.item.service.EquipmentCategoryDeleteItemService;
import jp.co.highjunk.equipment.api.equipment.category.delete.item.service.EquipmentCategoryDeleteItemServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * K-04 種別を消す（詳細設計 equipment/category/K-04_種別を消す.md）。
 */
@RestController
@RequiredArgsConstructor
public class EquipmentCategoryDeleteItemController {

	private final EquipmentCategoryDeleteItemService service;

	/** 消したら 200 で、data は null（API設計 2.4）。本文を受け取らないので、RestInDto・RestOutDto は作らない。 */
	@DeleteMapping("/api/categories/{id}")
	@PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
	public ApiResponse<Void> delete(@PathVariable long id, @AuthenticationPrincipal LoginUserId user) {
		service.delete(new EquipmentCategoryDeleteItemServiceInDto(id, user.getUserId()));
		return ApiResponse.ok(null);
	}
}
