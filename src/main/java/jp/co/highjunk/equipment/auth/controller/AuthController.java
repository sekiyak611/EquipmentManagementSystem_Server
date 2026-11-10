package jp.co.highjunk.equipment.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jp.co.highjunk.equipment.auth.service.AuthService;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginSession;
import jp.co.highjunk.equipment.common.security.LoginUserId;
import jp.co.highjunk.equipment.dto.auth.LoginRequest;
import jp.co.highjunk.equipment.dto.auth.MeResponse;

/**
 * auth 機能のAPIの入口（A-01〜A-03。詳細設計 auth/auth.md）。
 * パスワード関係の API（A-04〜A-07）も、このあとここに足す（Controller は1つの機能に1つ。CLAUDE.md 5章）。
 */
@RestController
public class AuthController {

	private final AuthService authService;

	private final LoginSession loginSession;

	public AuthController(AuthService authService, LoginSession loginSession) {
		this.authService = authService;
		this.loginSession = loginSession;
	}

	/**
	 * A-01 ログインする。ログイン前でも呼べる（common.security.SecurityConfig）。
	 * 成功したら、ログイン状態をセッションに入れ、data にログインした人の情報（A-03 と同じ形）を返す。
	 */
	@PostMapping("/api/auth/login")
	public ApiResponse<MeResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		MeResponse me = authService.login(request);
		loginSession.start(me.id(), me.permissions(), httpRequest, httpResponse);
		return ApiResponse.ok(me);
	}

	/** A-02 ログアウトする。 */
	@PostMapping("/api/auth/logout")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		loginSession.end(httpRequest, httpResponse);
		return ApiResponse.ok(null);
	}

	/** A-03 ログインしている人の情報と権限項目を返す。 */
	@GetMapping("/api/auth/me")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<MeResponse> me(@AuthenticationPrincipal LoginUserId user) {
		return ApiResponse.ok(authService.me(user.getUserId()));
	}
}
