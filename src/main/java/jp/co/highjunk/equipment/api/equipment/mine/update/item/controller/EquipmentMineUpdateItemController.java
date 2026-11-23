package jp.co.highjunk.equipment.api.equipment.mine.update.item.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.api.equipment.mine.update.item.model.EquipmentMineUpdateItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.mine.update.item.service.EquipmentMineUpdateItemService;
import jp.co.highjunk.equipment.api.equipment.mine.update.item.service.EquipmentMineUpdateItemServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * B-04 自分が借りている備品を「確認した」にする（詳細設計 equipment/mine/B-04_確認した.md）。
 */
@RestController
@RequiredArgsConstructor
public class EquipmentMineUpdateItemController {

	private final EquipmentMineUpdateItemService service;

	/**
	 * 送られた備品（1件でも、まとめてでも）の最終確認日を、今日にする。1つでも、自分が借りていなければ、どれも更新しない（403）。
	 * 200 で、data に、確認したあとの最新の「自分の備品」一覧（B-01 と同じ形）を入れて返す。
	 */
	@PostMapping("/api/me/confirmations")
	@PreAuthorize("hasAuthority('CONFIRM_SELF')")
	public ApiResponse<EquipmentMineUpdateItemRestOutDto> confirm(
			@Valid @RequestBody EquipmentMineUpdateItemRestInDto restInDto,
			@AuthenticationPrincipal LoginUserId user) {
		return ApiResponse.ok(service
				.confirm(new EquipmentMineUpdateItemServiceInDto(restInDto.getEquipmentIds(), user.getUserId())));
	}
}
