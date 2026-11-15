package jp.co.highjunk.equipment.api.auth.passwordtokens.verify.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A-05 で Service に渡す値（詳細設計 auth/A-05_合言葉の確認.md の2.5）。Controller が RestInDto から作る。
 */
@Getter
@AllArgsConstructor
public class AuthPasswordTokensVerifyServiceInDto {

	/** リンクの合言葉（ログに出さない） */
	private final String token;

	/** 合言葉をログなどに出さないよう、文字にするときは伏せる */
	@Override
	public String toString() {
		return "AuthPasswordTokensVerifyServiceInDto[]";
	}
}
