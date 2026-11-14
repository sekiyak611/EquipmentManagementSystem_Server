package jp.co.highjunk.equipment.api.auth.login.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A-01 で Service に渡す値（詳細設計 auth/A-01_ログイン.md の2.5）。Controller が RestInDto から作る。
 * 受け取った値だけで DB と照らして確かめることはない（パスワードの照らし合わせは、読んだユーザが要るので Validator で行う）。
 */
@Getter
@AllArgsConstructor
public class AuthLoginServiceInDto {

	/** メールアドレス（受け取ったまま。小文字にそろえるのは ServiceImpl） */
	private final String email;

	/** パスワード（ログに出さない） */
	private final String password;

	/** パスワードをログなどに出さないよう、文字にするときは伏せる */
	@Override
	public String toString() {
		return "AuthLoginServiceInDto[]";
	}
}
