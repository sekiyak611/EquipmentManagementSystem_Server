package jp.co.highjunk.equipment.api.auth.login.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * A-01 で画面から受け取る値と、形式のチェック（docs/API設計.md 4.1。メッセージは画面設計 2章のとおり）。
 */
@Getter
public class AuthLoginRestInDto {

	/** ログインに使うメールアドレス */
	@NotBlank(message = "メールアドレスを入力してください")
	@Size(max = 254, message = "メールアドレスの形が正しくありません")
	@Email(message = "メールアドレスの形が正しくありません")
	private final String email;

	/** パスワード（ログに出さない） */
	@NotBlank(message = "パスワードを入力してください")
	private final String password;

	/** 「ログインしたままにする」。今は受け取るだけで使わない（ZPOP-125） */
	private final Boolean rememberMe;

	@JsonCreator
	public AuthLoginRestInDto(@JsonProperty("email") String email, @JsonProperty("password") String password,
			@JsonProperty("rememberMe") Boolean rememberMe) {
		this.email = email;
		this.password = password;
		this.rememberMe = rememberMe;
	}

	/** パスワードをログなどに出さないよう、文字にするときはメールアドレスも含めて伏せる */
	@Override
	public String toString() {
		return "AuthLoginRestInDto[rememberMe=" + rememberMe + "]";
	}
}
