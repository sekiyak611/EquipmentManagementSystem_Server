package jp.co.highjunk.equipment.api.auth.update.password.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jp.co.highjunk.equipment.common.validation.NewPasswordFormat;
import jp.co.highjunk.equipment.common.validation.PasswordConfirmable;
import jp.co.highjunk.equipment.common.validation.PasswordConfirmed;
import lombok.Getter;

/**
 * A-07 で画面から受け取る値と、形式のチェック（詳細設計 auth/A-07_自分のパスワード変更.md の2.3・6章。メッセージは画面設計 7章・8章のとおり）。
 * パスワードは前後の空白も取らない（スペースも使えるため）。ログには出さない。
 */
@Getter
@PasswordConfirmed
public class AuthUpdatePasswordRestInDto implements PasswordConfirmable {

	/** 現在のパスワード。合っているかは、読んだユーザのハッシュが要るので Validator で確かめる */
	@NotEmpty(message = "現在のパスワードを入力してください")
	private final String currentPassword;

	/** 新しいパスワード */
	@NewPasswordFormat
	private final String password;

	/** 新しいパスワード（確認）。password と同じか、クラスの {@link PasswordConfirmed} で確かめる */
	private final String passwordConfirm;

	@JsonCreator
	public AuthUpdatePasswordRestInDto(@JsonProperty("currentPassword") String currentPassword,
			@JsonProperty("password") String password, @JsonProperty("passwordConfirm") String passwordConfirm) {
		this.currentPassword = currentPassword;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
	}

	/** パスワードをログなどに出さないよう、文字にするときは伏せる */
	@Override
	public String toString() {
		return "AuthUpdatePasswordRestInDto[]";
	}
}
