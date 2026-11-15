package jp.co.highjunk.equipment.api.auth.passwordtokens.verify.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.api.auth.passwordtokens.verify.model.AuthPasswordTokensVerifyRestOutDto;
import jp.co.highjunk.equipment.api.auth.passwordtokens.verify.service.AuthPasswordTokensVerifyService;
import jp.co.highjunk.equipment.api.auth.passwordtokens.verify.service.AuthPasswordTokensVerifyServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import lombok.RequiredArgsConstructor;

/**
 * A-05 リンクの合言葉が使えるか確かめる（詳細設計 auth/A-05_合言葉の確認.md）。
 * ログイン前でも呼べる（common.security.SecurityConfig）。合言葉は、アドレスではなく JSON の中で受け取る（API設計 3.1）。
 */
@RestController
@RequiredArgsConstructor
public class AuthPasswordTokensVerifyController {

	private final AuthPasswordTokensVerifyService service;

	/** 使えるなら 200 で、対象のメールアドレスと用途を返す。使えないなら 400（BusinessRuleException）。 */
	@PostMapping("/api/auth/password-tokens/verify")
	public ApiResponse<AuthPasswordTokensVerifyRestOutDto> verify(
			@RequestBody AuthPasswordTokensVerifyRestInDto restInDto) {
		return ApiResponse.ok(service.verify(new AuthPasswordTokensVerifyServiceInDto(restInDto.getToken())));
	}
}
