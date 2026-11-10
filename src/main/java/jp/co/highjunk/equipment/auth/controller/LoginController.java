package jp.co.highjunk.equipment.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jp.co.highjunk.equipment.auth.service.LoginService;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginSession;
import jp.co.highjunk.equipment.dto.auth.LoginRequest;
import jp.co.highjunk.equipment.dto.auth.MeResponse;

/**
 * A-01 ログインする（詳細設計 auth/A-01_ログイン.md）。ログイン前でも呼べる（common.security.SecurityConfig）。
 * controller は API 1本に1つ（CLAUDE.md 5章）。
 */
@RestController
public class LoginController {

	private final LoginService loginService;

	private final LoginSession loginSession;

	public LoginController(LoginService loginService, LoginSession loginSession) {
		this.loginService = loginService;
		this.loginSession = loginSession;
	}

	/**
	 * 成功したら、ログイン状態をセッションに入れ、data にログインした人の情報（A-03 と同じ形）を返す。
	 * LoginSession は、controller から直接呼んでよい例外（CLAUDE.md 5章）。
	 */
	@PostMapping("/api/auth/login")
	public ApiResponse<MeResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		MeResponse me = loginService.login(request);
		loginSession.start(me.id(), me.permissions(), httpRequest, httpResponse);
		return ApiResponse.ok(me);
	}
}
