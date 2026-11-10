package jp.co.highjunk.equipment.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.auth.service.MeService;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import jp.co.highjunk.equipment.dto.auth.MeResponse;

/**
 * A-03 ログインしている人の情報と権限項目を返す（詳細設計 auth/A-03_ログイン中の人の情報.md）。
 */
@RestController
public class MeController {

	private final MeService meService;

	public MeController(MeService meService) {
		this.meService = meService;
	}

	@GetMapping("/api/auth/me")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<MeResponse> me(@AuthenticationPrincipal LoginUserId user) {
		return ApiResponse.ok(meService.me(user.getUserId()));
	}
}
