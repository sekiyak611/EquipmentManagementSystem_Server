package jp.co.highjunk.equipment.api.auth.passwordtokens.use.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.api.auth.passwordtokens.use.service.AuthPasswordTokensUseService;
import jp.co.highjunk.equipment.api.auth.passwordtokens.use.service.AuthPasswordTokensUseServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import lombok.RequiredArgsConstructor;

/**
 * A-06 合言葉を使って新しいパスワードを決める（詳細設計 auth/A-06_合言葉でパスワードを決める.md）。
 * ログイン前でも呼べる（common.security.SecurityConfig）。自動ではログインさせない（機能一覧 6章の8）ので、LoginSession は呼ばない。
 */
@RestController
@RequiredArgsConstructor
public class AuthPasswordTokensUseController {

	private final AuthPasswordTokensUseService service;

	/** 決められたら 200（data は null）。合言葉が使えないなら 400、パスワードの決まりに合わないなら 400（入力の誤り）。 */
	@PostMapping("/api/auth/password-tokens/use")
	public ApiResponse<Void> use(@Valid @RequestBody AuthPasswordTokensUseRestInDto restInDto) {
		service.use(new AuthPasswordTokensUseServiceInDto(restInDto.getToken(), restInDto.getPassword()));
		return ApiResponse.ok(null);
	}
}
