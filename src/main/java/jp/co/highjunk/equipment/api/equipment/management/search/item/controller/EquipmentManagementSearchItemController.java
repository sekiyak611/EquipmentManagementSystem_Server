package jp.co.highjunk.equipment.api.equipment.management.search.item.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.api.equipment.management.search.item.model.EquipmentManagementSearchItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.management.search.item.service.EquipmentManagementSearchItemService;
import jp.co.highjunk.equipment.api.equipment.management.search.item.service.EquipmentManagementSearchItemServiceInDto;
import jp.co.highjunk.equipment.common.constant.ConfirmFilter;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatusFilter;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import lombok.RequiredArgsConstructor;

/**
 * C-01 備品を探す（詳細設計 equipment/management/C-01_備品を探す.md）。
 */
@RestController
@RequiredArgsConstructor
public class EquipmentManagementSearchItemController {

	private final EquipmentManagementSearchItemService service;

	/**
	 * 「備品一覧を見る」（EQUIPMENT_VIEW）を持つ人が呼べる。
	 * status・loanStatus・confirm は決まった値（大文字）でなければ、Spring が 400 にする（6章）。
	 * アドレスの後ろの値を RestInDto にまとめて確かめ、ServiceInDto に写して Service に渡す。
	 */
	@GetMapping("/api/equipment")
	@PreAuthorize("hasAuthority('EQUIPMENT_VIEW')")
	public ApiResponse<EquipmentManagementSearchItemRestOutDto> search(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(name = "status", required = false) List<EquipmentStatus> statuses,
			@RequestParam(required = false) LoanStatusFilter loanStatus,
			@RequestParam(required = false) ConfirmFilter confirm,
			@RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "1") int page) {
		EquipmentManagementSearchItemRestInDto restInDto = EquipmentManagementSearchItemRestInDto.of(keyword,
				categoryId, statuses, loanStatus, confirm, sort, page);
		EquipmentManagementSearchItemServiceInDto serviceInDto = new EquipmentManagementSearchItemServiceInDto(
				restInDto.getKeyword(), restInDto.getCategoryId(), restInDto.getStatuses(), restInDto.getLoanStatus(),
				restInDto.getConfirm(), restInDto.getSort(), restInDto.isDescending(), restInDto.getPage());
		return ApiResponse.ok(service.search(serviceInDto));
	}
}
