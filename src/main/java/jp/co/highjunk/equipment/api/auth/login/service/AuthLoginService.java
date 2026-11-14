package jp.co.highjunk.equipment.api.auth.login.service;

import jp.co.highjunk.equipment.api.auth.login.model.AuthLoginRestOutDto;
import jp.co.highjunk.equipment.common.error.UnauthorizedException;

/**
 * A-01 ログインの業務の処理（中身は AuthLoginServiceImpl。詳細設計 auth/A-01_ログイン.md）。
 */
public interface AuthLoginService {

	/**
	 * メールアドレスとパスワードを確かめ、ログインした人の情報を返す（セッションに入れるのは Controller が LoginSession で行う）。
	 *
	 * @throws UnauthorizedException ログインできないとき（401）
	 */
	AuthLoginRestOutDto login(AuthLoginServiceInDto serviceInDto);
}
