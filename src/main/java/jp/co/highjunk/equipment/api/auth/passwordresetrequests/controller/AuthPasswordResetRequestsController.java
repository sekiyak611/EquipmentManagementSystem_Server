package jp.co.highjunk.equipment.api.auth.passwordresetrequests.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.api.auth.passwordresetrequests.service.AuthPasswordResetRequestsService;
import jp.co.highjunk.equipment.api.auth.passwordresetrequests.service.AuthPasswordResetRequestsServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import lombok.RequiredArgsConstructor;

/**
 * A-04 パスワード設定用のリンクを依頼する（詳細設計 auth/A-04_パスワード設定リンクの依頼.md）。
 * ログイン前でも呼べる（common.security.SecurityConfig）。
 */
@RestController
@RequiredArgsConstructor
public class AuthPasswordResetRequestsController {

	private final AuthPasswordResetRequestsService service;

	/**
	 * 登録の有無・5分以内の依頼・DMを送れたかにかかわらず、いつも 202（受け付けた）を返す。
	 * 登録されているかどうかを、他人が調べられないようにするため（画面設計 5章）。
	 */
	@PostMapping("/api/auth/password-reset-requests")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ApiResponse<Void> request(@Valid @RequestBody AuthPasswordResetRequestsRestInDto restInDto) {
		service.request(new AuthPasswordResetRequestsServiceInDto(restInDto.getEmail()));
		return ApiResponse.accepted();
	}
}
