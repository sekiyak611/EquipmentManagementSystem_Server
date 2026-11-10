package jp.co.highjunk.equipment.auth.service;

import jp.co.highjunk.equipment.common.error.UnauthorizedException;
import jp.co.highjunk.equipment.dto.auth.LoginRequest;
import jp.co.highjunk.equipment.dto.auth.MeResponse;

/**
 * auth 機能の業務のルールの形（詳細設計 auth/auth.md）。中身は AuthServiceImpl。
 */
public interface AuthService {

	/**
	 * A-01 メールアドレスとパスワードを確かめ、ログインした人の情報を返す（セッションに入れるのは controller が LoginSession で行う）。
	 *
	 * @throws UnauthorizedException ログインできないとき（401）
	 */
	MeResponse login(LoginRequest request);

	/**
	 * A-03 ログインしている人の情報と権限項目を返す。
	 *
	 * @throws UnauthorizedException ユーザが見つからない（削除された）とき（401）
	 */
	MeResponse me(long userId);
}
