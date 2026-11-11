package jp.co.highjunk.equipment.equipment.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import jp.co.highjunk.equipment.dto.equipment.EquipmentListItem;
import jp.co.highjunk.equipment.equipment.service.EquipmentConfirmService;

/**
 * C-06 備品を「確認した」にする（詳細設計 equipment/C-06_確認した.md）。
 */
@RestController
public class EquipmentConfirmController {

	private final EquipmentConfirmService equipmentConfirmService;

	public EquipmentConfirmController(EquipmentConfirmService equipmentConfirmService) {
		this.equipmentConfirmService = equipmentConfirmService;
	}

	/** 本文は受け取らない。200 で、data に変えたあとの備品（C-01 の1件と同じ形）を入れて返す。 */
	@PostMapping("/api/equipment/{id}/confirmation")
	@PreAuthorize("hasAuthority('CONFIRM_ALL')")
	public ApiResponse<EquipmentListItem> confirm(@PathVariable long id, @AuthenticationPrincipal LoginUserId user) {
		return ApiResponse.ok(equipmentConfirmService.confirm(id, user.getUserId()));
	}
}
