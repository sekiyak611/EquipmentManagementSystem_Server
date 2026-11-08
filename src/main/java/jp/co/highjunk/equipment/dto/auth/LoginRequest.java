package jp.co.highjunk.equipment.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A-01 で受け取る値（docs/API設計.md 4.1）。入力チェックとメッセージは画面設計 2章のとおり。
 *
 * @param email      ログインに使うメールアドレス
 * @param password   パスワード（ログに出さない）
 * @param rememberMe 「ログインしたままにする」。今は受け取るだけで使わない（ZPOP-125）
 */
public record LoginRequest(
		@NotBlank(message = "メールアドレスを入力してください")
		@Size(max = 254, message = "メールアドレスの形が正しくありません")
		@Email(message = "メールアドレスの形が正しくありません")
		String email,

		@NotBlank(message = "パスワードを入力してください")
		String password,

		Boolean rememberMe) {

	/** パスワードをログなどに出さないよう、文字にするときはメールアドレスも含めて伏せる */
	@Override
	public String toString() {
		return "LoginRequest[rememberMe=" + rememberMe + "]";
	}
}
