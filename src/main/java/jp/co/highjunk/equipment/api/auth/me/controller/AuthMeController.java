package jp.co.highjunk.equipment.api.auth.me.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.api.auth.me.model.AuthMeRestOutDto;
import jp.co.highjunk.equipment.api.auth.me.service.AuthMeService;
import jp.co.highjunk.equipment.api.auth.me.service.AuthMeServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * A-03 ログインしている人の情報と権限項目を返す（詳細設計 auth/A-03_ログイン中の人の情報.md）。
 */
@RestController
@RequiredArgsConstructor
public class AuthMeController {

	private final AuthMeService service;

	/** 本文を受け取らないので RestInDto は作らず、ログインしている人のユーザIDから ServiceInDto を作る。 */
	@GetMapping("/api/auth/me")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<AuthMeRestOutDto> me(@AuthenticationPrincipal LoginUserId user) {
		return ApiResponse.ok(service.me(new AuthMeServiceInDto(user.getUserId())));
	}
}
