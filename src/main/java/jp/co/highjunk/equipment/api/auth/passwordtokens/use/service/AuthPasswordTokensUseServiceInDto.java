package jp.co.highjunk.equipment.api.auth.passwordtokens.use.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A-06 で Service に渡す値（詳細設計 auth/A-06_合言葉でパスワードを決める.md の2.5）。Controller が RestInDto から作る。
 * 確認の欄は、RestInDto で確かめ終わっているので渡さない。
 */
@Getter
@AllArgsConstructor
public class AuthPasswordTokensUseServiceInDto {

	/** リンクの合言葉（ログに出さない） */
	private final String token;

	/** 新しいパスワード（ログに出さない） */
	private final String password;

	/** 合言葉・パスワードをログなどに出さないよう、文字にするときは伏せる */
	@Override
	public String toString() {
		return "AuthPasswordTokensUseServiceInDto[]";
	}
}
