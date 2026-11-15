package jp.co.highjunk.equipment.api.auth.passwordtokens.use.validator;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AuthPasswordTokensUseValidator に渡す値。ServiceImpl 側（PasswordSetter）が読んだユーザのメールアドレスと、新しいパスワードを入れる。
 */
@Getter
@AllArgsConstructor
public class AuthPasswordTokensUseValidatorDto {

	/** 合言葉のユーザのメールアドレス */
	private final String email;

	/** 新しいパスワード（ログに出さない） */
	private final String password;

	/** パスワードをログなどに出さないよう、文字にするときは伏せる */
	@Override
	public String toString() {
		return "AuthPasswordTokensUseValidatorDto[]";
	}
}
