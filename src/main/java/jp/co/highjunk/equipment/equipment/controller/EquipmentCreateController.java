package jp.co.highjunk.equipment.equipment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import jp.co.highjunk.equipment.dto.equipment.EquipmentCreateRequest;
import jp.co.highjunk.equipment.dto.equipment.EquipmentListItem;
import jp.co.highjunk.equipment.equipment.service.EquipmentCreateService;

/**
 * C-03 備品を登録する（詳細設計 equipment/C-03_備品を登録する.md）。
 */
@RestController
public class EquipmentCreateController {

	private final EquipmentCreateService equipmentCreateService;

	public EquipmentCreateController(EquipmentCreateService equipmentCreateService) {
		this.equipmentCreateService = equipmentCreateService;
	}

	/** 登録したら 201 で、data に作った備品（C-01 の1件と同じ形）を入れて返す。 */
	@PostMapping("/api/equipment")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('EQUIPMENT_CREATE')")
	public ApiResponse<EquipmentListItem> create(@Valid @RequestBody EquipmentCreateRequest request,
			@AuthenticationPrincipal LoginUserId user) {
		return ApiResponse.created(equipmentCreateService.create(request, user.getUserId()));
	}
}
