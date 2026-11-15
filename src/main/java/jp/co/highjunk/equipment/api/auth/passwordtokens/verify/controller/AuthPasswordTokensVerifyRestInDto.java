package jp.co.highjunk.equipment.api.auth.passwordtokens.verify.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * A-05 で画面から受け取る値（詳細設計 auth/A-05_合言葉の確認.md の2.3）。
 * 形式のチェックはしない。空・ないときも、「使えないリンク」として ServiceImpl 側で扱う（6章）。
 */
@Getter
public class AuthPasswordTokensVerifyRestInDto {

	/** リンクの合言葉（ログに出さない） */
	private final String token;

	@JsonCreator
	public AuthPasswordTokensVerifyRestInDto(@JsonProperty("token") String token) {
		this.token = token;
	}

	/** 合言葉をログなどに出さないよう、文字にするときは伏せる */
	@Override
	public String toString() {
		return "AuthPasswordTokensVerifyRestInDto[]";
	}
}
