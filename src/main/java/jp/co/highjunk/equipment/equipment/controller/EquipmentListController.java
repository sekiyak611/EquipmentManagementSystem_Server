package jp.co.highjunk.equipment.equipment.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.common.constant.ConfirmFilter;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatusFilter;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.dto.equipment.EquipmentListRequest;
import jp.co.highjunk.equipment.dto.equipment.EquipmentListResponse;
import jp.co.highjunk.equipment.equipment.service.EquipmentListService;

/**
 * C-01 備品を探す（詳細設計 equipment/C-01_備品を探す.md）。
 */
@RestController
public class EquipmentListController {

	private final EquipmentListService equipmentListService;

	public EquipmentListController(EquipmentListService equipmentListService) {
		this.equipmentListService = equipmentListService;
	}

	/**
	 * 「備品一覧を見る」（EQUIPMENT_VIEW）を持つ人が呼べる。
	 * status・loanStatus・confirm は決まった値（大文字）でなければ、Spring が 400 にする（6章）。
	 */
	@GetMapping("/api/equipment")
	@PreAuthorize("hasAuthority('EQUIPMENT_VIEW')")
	public ApiResponse<EquipmentListResponse> list(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(name = "status", required = false) List<EquipmentStatus> statuses,
			@RequestParam(required = false) LoanStatusFilter loanStatus,
			@RequestParam(required = false) ConfirmFilter confirm,
			@RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "1") int page) {
		EquipmentListRequest request = EquipmentListRequest.of(keyword, categoryId, statuses, loanStatus, confirm,
				sort, page);
		return ApiResponse.ok(equipmentListService.list(request));
	}
}
