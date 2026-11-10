package jp.co.highjunk.equipment.category.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.category.service.CategoryListService;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.dto.category.CategoryResponse;

/**
 * K-01 種別の一覧（詳細設計 category/K-01_種別一覧.md）。
 */
@RestController
public class CategoryListController {

	private final CategoryListService categoryListService;

	public CategoryListController(CategoryListService categoryListService) {
		this.categoryListService = categoryListService;
	}

	/**
	 * ログインしていればだれでも呼べる。data に種別の配列をそのまま入れて返す（ページ分けはしない。K-01 の12章）。
	 */
	@GetMapping("/api/categories")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<CategoryResponse>> list() {
		return ApiResponse.ok(categoryListService.list());
	}
}
