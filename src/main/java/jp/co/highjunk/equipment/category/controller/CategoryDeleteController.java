package jp.co.highjunk.equipment.category.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.category.service.CategoryDeleteService;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;

/**
 * K-04 種別を消す（詳細設計 category/K-04_種別を消す.md）。
 */
@RestController
public class CategoryDeleteController {

	private final CategoryDeleteService categoryDeleteService;

	public CategoryDeleteController(CategoryDeleteService categoryDeleteService) {
		this.categoryDeleteService = categoryDeleteService;
	}

	/** 消したら 200 で、data は null（API設計 2.4）。 */
	@DeleteMapping("/api/categories/{id}")
	@PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
	public ApiResponse<Void> delete(@PathVariable long id, @AuthenticationPrincipal LoginUserId user) {
		categoryDeleteService.delete(id, user.getUserId());
		return ApiResponse.ok(null);
	}
}
