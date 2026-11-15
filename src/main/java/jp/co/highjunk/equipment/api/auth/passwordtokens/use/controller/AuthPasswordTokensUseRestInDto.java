package jp.co.highjunk.equipment.api.auth.passwordtokens.use.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jp.co.highjunk.equipment.common.validation.NewPasswordFormat;
import jp.co.highjunk.equipment.common.validation.PasswordConfirmable;
import jp.co.highjunk.equipment.common.validation.PasswordConfirmed;
import lombok.Getter;

/**
 * A-06 で画面から受け取る値と、形式のチェック（docs/API設計.md 4.4。メッセージは画面設計 6章・8章のとおり）。
 * パスワードは前後の空白も取らない（スペースも使えるため）。合言葉・パスワードは、ログに出さない。
 */
@Getter
@PasswordConfirmed
public class AuthPasswordTokensUseRestInDto implements PasswordConfirmable {

	/** リンクの合言葉。形式のチェックはしない（空・ないときも、「使えないリンク」として扱う） */
	private final String token;

	/** 新しいパスワード */
	@NewPasswordFormat
	private final String password;

	/** 新しいパスワード（確認）。password と同じか、クラスの {@link PasswordConfirmed} で確かめる */
	private final String passwordConfirm;

	@JsonCreator
	public AuthPasswordTokensUseRestInDto(@JsonProperty("token") String token,
			@JsonProperty("password") String password, @JsonProperty("passwordConfirm") String passwordConfirm) {
		this.token = token;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
	}

	/** 合言葉・パスワードをログなどに出さないよう、文字にするときは伏せる */
	@Override
	public String toString() {
		return "AuthPasswordTokensUseRestInDto[]";
	}
}
