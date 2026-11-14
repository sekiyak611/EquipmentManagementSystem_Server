package jp.co.highjunk.equipment.api.auth.me.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A-03 で Service に渡す値（詳細設計 auth/A-03_ログイン中の人の情報.md の2.5）。Controller がログインしている人のユーザIDから作る。
 * ユーザがいないときは 401（404 ではない）なので、チェック用アノテーションは付けず、ServiceImpl で確かめる。
 */
@Getter
@AllArgsConstructor
public class AuthMeServiceInDto {

	/** ログインしている人のユーザID */
	private final long loginUserId;
}
