package jp.co.highjunk.equipment.api.equipment.category.update.item.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.api.equipment.category.update.item.model.EquipmentCategoryUpdateItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.category.update.item.service.EquipmentCategoryUpdateItemService;
import jp.co.highjunk.equipment.api.equipment.category.update.item.service.EquipmentCategoryUpdateItemServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * K-03 種別を変える（詳細設計 equipment/category/K-03_種別を変える.md）。
 */
@RestController
@RequiredArgsConstructor
public class EquipmentCategoryUpdateItemController {

	private final EquipmentCategoryUpdateItemService service;

	/** 変えたら 200 で、data に変えた種別を入れて返す。 */
	@PutMapping("/api/categories/{id}")
	@PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
	public ApiResponse<EquipmentCategoryUpdateItemRestOutDto> update(@PathVariable long id,
			@Valid @RequestBody EquipmentCategoryUpdateItemRestInDto restInDto,
			@AuthenticationPrincipal LoginUserId user) {
		EquipmentCategoryUpdateItemServiceInDto serviceInDto = new EquipmentCategoryUpdateItemServiceInDto(id,
				restInDto.getName(), restInDto.getInitial(), restInDto.getVersion(), user.getUserId());
		return ApiResponse.ok(service.update(serviceInDto));
	}
}
