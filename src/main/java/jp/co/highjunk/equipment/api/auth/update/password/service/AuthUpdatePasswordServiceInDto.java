package jp.co.highjunk.equipment.api.auth.update.password.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A-07 で Service に渡す値（詳細設計 auth/A-07_自分のパスワード変更.md の2.5）。Controller が、RestInDto とログインしている人から作る。
 * 確認の欄は、RestInDto で確かめ終わっているので渡さない。
 */
@Getter
@AllArgsConstructor
public class AuthUpdatePasswordServiceInDto {

	/** ログインしている人（変える相手）のユーザID */
	private final long userId;

	/** 現在のパスワード（ログに出さない） */
	private final String currentPassword;

	/** 新しいパスワード（ログに出さない） */
	private final String password;

	/** パスワードをログなどに出さないよう、文字にするときは伏せる */
	@Override
	public String toString() {
		return "AuthUpdatePasswordServiceInDto[userId=" + userId + "]";
	}
}
