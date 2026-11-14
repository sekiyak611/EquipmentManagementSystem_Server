package jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem.model.EquipmentManagementUpdateConfirmationItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem.service.EquipmentManagementUpdateConfirmationItemService;
import jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem.service.EquipmentManagementUpdateConfirmationItemServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * C-06 備品を「確認した」にする（詳細設計 equipment/management/C-06_確認した.md）。
 */
@RestController
@RequiredArgsConstructor
public class EquipmentManagementUpdateConfirmationItemController {

	private final EquipmentManagementUpdateConfirmationItemService service;

	/** 本文は受け取らない（RestInDto は作らない）。200 で、data に変えたあとの備品（C-01 の1件と同じ形）を入れて返す。 */
	@PostMapping("/api/equipment/{id}/confirmation")
	@PreAuthorize("hasAuthority('CONFIRM_ALL')")
	public ApiResponse<EquipmentManagementUpdateConfirmationItemRestOutDto> confirm(@PathVariable long id,
			@AuthenticationPrincipal LoginUserId user) {
		return ApiResponse.ok(service.confirm(new EquipmentManagementUpdateConfirmationItemServiceInDto(id,
				user.getUserId())));
	}
}
