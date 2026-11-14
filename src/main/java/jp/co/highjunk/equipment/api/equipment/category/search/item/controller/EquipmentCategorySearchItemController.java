package jp.co.highjunk.equipment.api.equipment.category.search.item.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.api.equipment.category.search.item.model.EquipmentCategorySearchItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.category.search.item.service.EquipmentCategorySearchItemService;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import lombok.RequiredArgsConstructor;

/**
 * K-01 種別の一覧（詳細設計 equipment/category/K-01_種別一覧.md）。
 */
@RestController
@RequiredArgsConstructor
public class EquipmentCategorySearchItemController {

	private final EquipmentCategorySearchItemService service;

	/**
	 * ログインしていればだれでも呼べる。data に種別の配列をそのまま入れて返す（ページ分けはしない。K-01 の12章）。
	 * 受け取る値がないので、ServiceInDto は作らない（CLAUDE.md 5章）。
	 */
	@GetMapping("/api/categories")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<EquipmentCategorySearchItemRestOutDto>> search() {
		return ApiResponse.ok(service.search());
	}
}
