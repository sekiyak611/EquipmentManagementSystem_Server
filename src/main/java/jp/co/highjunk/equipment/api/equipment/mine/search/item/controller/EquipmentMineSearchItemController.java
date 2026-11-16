package jp.co.highjunk.equipment.api.equipment.mine.search.item.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.api.equipment.mine.search.item.model.EquipmentMineSearchItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.mine.search.item.service.EquipmentMineSearchItemService;
import jp.co.highjunk.equipment.api.equipment.mine.search.item.service.EquipmentMineSearchItemServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * B-01 自分が借りている備品（詳細設計 equipment/mine/B-01_自分の備品の一覧.md）。
 */
@RestController
@RequiredArgsConstructor
public class EquipmentMineSearchItemController {

	private final EquipmentMineSearchItemService service;

	/** 受け取る値はない（RestInDto は作らない）。ログインしている人の分だけを返す。 */
	@GetMapping("/api/me/loans")
	@PreAuthorize("hasAuthority('LOAN_SELF')")
	public ApiResponse<EquipmentMineSearchItemRestOutDto> search(@AuthenticationPrincipal LoginUserId user) {
		return ApiResponse.ok(service.search(new EquipmentMineSearchItemServiceInDto(user.getUserId())));
	}
}
