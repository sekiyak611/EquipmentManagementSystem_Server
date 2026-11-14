package jp.co.highjunk.equipment.api.equipment.management.entry.item.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.api.equipment.management.entry.item.model.EquipmentManagementEntryItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.management.entry.item.service.EquipmentManagementEntryItemService;
import jp.co.highjunk.equipment.api.equipment.management.entry.item.service.EquipmentManagementEntryItemServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * C-03 備品を登録する（詳細設計 equipment/management/C-03_備品を登録する.md）。
 */
@RestController
@RequiredArgsConstructor
public class EquipmentManagementEntryItemController {

	private final EquipmentManagementEntryItemService service;

	/** 登録したら 201 で、data に作った備品（C-01 の1件と同じ形）を入れて返す。 */
	@PostMapping("/api/equipment")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('EQUIPMENT_CREATE')")
	public ApiResponse<EquipmentManagementEntryItemRestOutDto> entry(
			@Valid @RequestBody EquipmentManagementEntryItemRestInDto restInDto,
			@AuthenticationPrincipal LoginUserId user) {
		EquipmentManagementEntryItemServiceInDto serviceInDto = new EquipmentManagementEntryItemServiceInDto(
				restInDto.getManagementNo(), restInDto.getCategoryId(), restInDto.getMaker(), restInDto.getModel(),
				restInDto.getSerialNo(), restInDto.getSize(), restInDto.getResolution(),
				restInDto.getHeightAdjustable(), restInDto.getPivot(), restInDto.getPurchasedOn(),
				restInDto.getWarrantyUntil(), restInDto.getNote(), user.getUserId());
		return ApiResponse.created(service.entry(serviceInDto));
	}
}
