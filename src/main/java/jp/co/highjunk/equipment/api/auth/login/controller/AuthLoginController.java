package jp.co.highjunk.equipment.api.auth.login.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jp.co.highjunk.equipment.api.auth.login.model.AuthLoginRestOutDto;
import jp.co.highjunk.equipment.api.auth.login.service.AuthLoginService;
import jp.co.highjunk.equipment.api.auth.login.service.AuthLoginServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginSession;
import lombok.RequiredArgsConstructor;

/**
 * A-01 ログインする（詳細設計 auth/A-01_ログイン.md）。ログイン前でも呼べる（common.security.SecurityConfig）。
 */
@RestController
@RequiredArgsConstructor
public class AuthLoginController {

	private final AuthLoginService service;

	private final LoginSession loginSession;

	/**
	 * 成功したら、ログイン状態をセッションに入れ、data にログインした人の情報（A-03 と同じ形）を返す。
	 * LoginSession は、Controller から直接呼んでよい例外（CLAUDE.md 5章）。
	 */
	@PostMapping("/api/auth/login")
	public ApiResponse<AuthLoginRestOutDto> login(@Valid @RequestBody AuthLoginRestInDto restInDto,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		AuthLoginRestOutDto restOutDto = service
				.login(new AuthLoginServiceInDto(restInDto.getEmail(), restInDto.getPassword()));
		loginSession.start(restOutDto.getId(), restOutDto.getPermissions(), httpRequest, httpResponse);
		return ApiResponse.ok(restOutDto);
	}
}
