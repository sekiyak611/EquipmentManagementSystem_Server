package jp.co.highjunk.equipment.api.equipment.category.entry.item.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.api.equipment.category.entry.item.model.EquipmentCategoryEntryItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.category.entry.item.service.EquipmentCategoryEntryItemService;
import jp.co.highjunk.equipment.api.equipment.category.entry.item.service.EquipmentCategoryEntryItemServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * K-02 種別を追加する（詳細設計 equipment/category/K-02_種別を追加する.md）。
 */
@RestController
@RequiredArgsConstructor
public class EquipmentCategoryEntryItemController {

	private final EquipmentCategoryEntryItemService service;

	/** 作ったら 201 で、data に作った種別を入れて返す。 */
	@PostMapping("/api/categories")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
	public ApiResponse<EquipmentCategoryEntryItemRestOutDto> entry(
			@Valid @RequestBody EquipmentCategoryEntryItemRestInDto restInDto,
			@AuthenticationPrincipal LoginUserId user) {
		EquipmentCategoryEntryItemServiceInDto serviceInDto = new EquipmentCategoryEntryItemServiceInDto(
				restInDto.getName(), restInDto.getInitial(), user.getUserId());
		return ApiResponse.created(service.entry(serviceInDto));
	}
}
