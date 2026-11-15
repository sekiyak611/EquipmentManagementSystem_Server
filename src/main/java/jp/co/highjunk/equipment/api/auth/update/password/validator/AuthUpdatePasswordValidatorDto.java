package jp.co.highjunk.equipment.api.auth.update.password.validator;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AuthUpdatePasswordValidator に渡す値。ServiceImpl が読んだユーザのメールアドレスとハッシュと、受け取ったパスワードを入れる。
 */
@Getter
@AllArgsConstructor
public class AuthUpdatePasswordValidatorDto {

	/** ログインしている人のメールアドレス */
	private final String email;

	/** 保存している現在のパスワードのハッシュ（ない人は null） */
	private final String passwordHash;

	/** 入力された現在のパスワード（ログに出さない） */
	private final String currentPassword;

	/** 入力された新しいパスワード（ログに出さない） */
	private final String password;

	/** パスワード・ハッシュをログなどに出さないよう、文字にするときは伏せる */
	@Override
	public String toString() {
		return "AuthUpdatePasswordValidatorDto[]";
	}
}
