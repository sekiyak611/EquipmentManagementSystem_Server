package jp.co.highjunk.equipment.api.auth.update.password.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jp.co.highjunk.equipment.api.auth.update.password.service.AuthUpdatePasswordAccountLockedException;
import jp.co.highjunk.equipment.api.auth.update.password.service.AuthUpdatePasswordService;
import jp.co.highjunk.equipment.api.auth.update.password.service.AuthUpdatePasswordServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginSession;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * A-07 自分のパスワードを変える（詳細設計 auth/A-07_自分のパスワード変更.md）。ログインしている人なら、だれでも呼べる。
 * LoginSession は、Controller から直接呼んでよい例外（CLAUDE.md 5章）。
 */
@RestController
@RequiredArgsConstructor
public class AuthUpdatePasswordController {

	private final AuthUpdatePasswordService service;

	private final LoginSession loginSession;

	/**
	 * 変えられたら、この端末のログインを新しくして（ほかの端末は切れる）、200 を返す。
	 * 現在のパスワードを間違えて、いまロックしたときは、この端末をログアウトさせてから、同じ 400 を返す（画面設計 7章）。
	 */
	@PutMapping("/api/me/password")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<Void> update(@Valid @RequestBody AuthUpdatePasswordRestInDto restInDto,
			@AuthenticationPrincipal LoginUserId user, Authentication authentication, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		// この端末が「ログインしたまま」なら、パスワードを変えたあとも、この端末の分は作り直す（A-07 の5章 No.5）
		boolean remembered = loginSession.isRemembered(httpRequest);
		try {
			service.update(new AuthUpdatePasswordServiceInDto(user.getUserId(), restInDto.getCurrentPassword(),
					restInDto.getPassword()));
		} catch (AuthUpdatePasswordAccountLockedException e) {
			loginSession.end(httpRequest, httpResponse);
			throw e;
		}
		List<String> permissions = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.toList();
		loginSession.start(user.getUserId(), permissions, remembered, httpRequest, httpResponse);
		return ApiResponse.ok(null);
	}
}
