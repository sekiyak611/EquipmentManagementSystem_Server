package jp.co.highjunk.equipment.category.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.category.service.CategoryCreateService;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import jp.co.highjunk.equipment.dto.category.CategoryCreateRequest;
import jp.co.highjunk.equipment.dto.category.CategoryResponse;

/**
 * K-02 種別を追加する（詳細設計 category/K-02_種別を追加する.md）。
 */
@RestController
public class CategoryCreateController {

	private final CategoryCreateService categoryCreateService;

	public CategoryCreateController(CategoryCreateService categoryCreateService) {
		this.categoryCreateService = categoryCreateService;
	}

	/** 作ったら 201 で、data に作った種別を入れて返す。 */
	@PostMapping("/api/categories")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
	public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest request,
			@AuthenticationPrincipal LoginUserId user) {
		return ApiResponse.created(categoryCreateService.create(request, user.getUserId()));
	}
}
