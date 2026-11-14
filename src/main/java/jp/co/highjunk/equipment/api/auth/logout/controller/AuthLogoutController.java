package jp.co.highjunk.equipment.api.auth.logout.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.common.security.LoginSession;
import lombok.RequiredArgsConstructor;

/**
 * A-02 ログアウトする（詳細設計 auth/A-02_ログアウト.md）。
 * 業務のルールがなく、セッションを捨てるだけなので、Service は作らず LoginSession を直接呼ぶ（CLAUDE.md 5章の例外）。
 * 受け取る値・返す値もないので、controller のパッケージだけを作る（CLAUDE.md 4.1）。
 */
@RestController
@RequiredArgsConstructor
public class AuthLogoutController {

	private final LoginSession loginSession;

	@PostMapping("/api/auth/logout")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		loginSession.end(httpRequest, httpResponse);
		return ApiResponse.ok(null);
	}
}
