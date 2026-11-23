package jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.model.EquipmentMineUpdateReturnItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.service.EquipmentMineUpdateReturnItemService;
import jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.service.EquipmentMineUpdateReturnItemServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * B-03 備品を返す（詳細設計 equipment/mine/B-03_備品を返す.md）。
 */
@RestController
@RequiredArgsConstructor
public class EquipmentMineUpdateReturnItemController {

	private final EquipmentMineUpdateReturnItemService service;

	/**
	 * 借りているのが自分の備品を返す。200 で、data に、返したあとの最新の「自分の備品」一覧（B-01 と同じ形）を入れて返す。
	 * 本文は、なくてもよい（メモなしの返却）。
	 */
	@PostMapping("/api/me/loans/{loanId}/return")
	@PreAuthorize("hasAuthority('LOAN_SELF')")
	public ApiResponse<EquipmentMineUpdateReturnItemRestOutDto> returnItem(@PathVariable long loanId,
			@Valid @RequestBody(required = false) EquipmentMineUpdateReturnItemRestInDto restInDto,
			@AuthenticationPrincipal LoginUserId user) {
		String memo = restInDto == null ? null : restInDto.getMemo();
		return ApiResponse.ok(service.returnItem(
				new EquipmentMineUpdateReturnItemServiceInDto(loanId, memo, user.getUserId())));
	}
}
