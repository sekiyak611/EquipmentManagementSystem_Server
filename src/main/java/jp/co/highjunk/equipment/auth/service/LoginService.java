package jp.co.highjunk.equipment.auth.service;

import jp.co.highjunk.equipment.common.error.UnauthorizedException;
import jp.co.highjunk.equipment.dto.auth.LoginRequest;
import jp.co.highjunk.equipment.dto.auth.MeResponse;

/**
 * A-01 ログインの業務のルール（中身は LoginServiceImpl。詳細設計 auth/A-01_ログイン.md）。
 */
public interface LoginService {

	/**
	 * メールアドレスとパスワードを確かめ、ログインした人の情報を返す（セッションに入れるのは controller が LoginSession で行う）。
	 *
	 * @throws UnauthorizedException ログインできないとき（401）
	 */
	MeResponse login(LoginRequest request);
}
