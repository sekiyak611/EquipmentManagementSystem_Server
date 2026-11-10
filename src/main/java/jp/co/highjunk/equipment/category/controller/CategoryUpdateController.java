package jp.co.highjunk.equipment.category.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.category.service.CategoryUpdateService;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import jp.co.highjunk.equipment.dto.category.CategoryResponse;
import jp.co.highjunk.equipment.dto.category.CategoryUpdateRequest;

/**
 * K-03 種別を変える（詳細設計 category/K-03_種別を変える.md）。
 */
@RestController
public class CategoryUpdateController {

	private final CategoryUpdateService categoryUpdateService;

	public CategoryUpdateController(CategoryUpdateService categoryUpdateService) {
		this.categoryUpdateService = categoryUpdateService;
	}

	/** 変えたら 200 で、data に変えた種別を入れて返す。 */
	@PutMapping("/api/categories/{id}")
	@PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
	public ApiResponse<CategoryResponse> update(@PathVariable long id, @Valid @RequestBody CategoryUpdateRequest request,
			@AuthenticationPrincipal LoginUserId user) {
		return ApiResponse.ok(categoryUpdateService.update(id, request, user.getUserId()));
	}
}
