package jp.co.highjunk.equipment.api.auth.login.validator;

import jp.co.highjunk.equipment.dao.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A-01 の業務チェック（AuthLoginValidator）に渡す値。ServiceImpl が、読んだユーザと受け取ったパスワードを入れる（CLAUDE.md 4.1）。
 */
@Getter
@AllArgsConstructor
public class AuthLoginValidatorDto {

	/** メールアドレスで読んだユーザ。登録がない・削除されたときは null */
	private final User user;

	/** 受け取ったパスワード（ログに出さない） */
	private final String password;

	/** パスワードをログなどに出さないよう、文字にするときは伏せる */
	@Override
	public String toString() {
		return "AuthLoginValidatorDto[]";
	}
}
