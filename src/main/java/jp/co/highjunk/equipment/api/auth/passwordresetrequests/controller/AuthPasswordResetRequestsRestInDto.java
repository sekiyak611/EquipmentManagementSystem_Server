package jp.co.highjunk.equipment.api.auth.passwordresetrequests.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * A-04 で画面から受け取る値と、形式のチェック（docs/API設計.md 4.3。メッセージは画面設計 5章のとおり）。
 */
@Getter
public class AuthPasswordResetRequestsRestInDto {

	/** パスワード設定用のリンクを受け取るメールアドレス */
	@NotBlank(message = "メールアドレスを入力してください")
	@Size(max = 254, message = "メールアドレスの形が正しくありません")
	@Email(message = "メールアドレスの形が正しくありません")
	private final String email;

	@JsonCreator
	public AuthPasswordResetRequestsRestInDto(@JsonProperty("email") String email) {
		this.email = email;
	}
}
