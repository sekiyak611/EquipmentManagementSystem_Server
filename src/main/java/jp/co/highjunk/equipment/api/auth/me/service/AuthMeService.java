package jp.co.highjunk.equipment.api.auth.me.service;

import jp.co.highjunk.equipment.api.auth.me.model.AuthMeRestOutDto;
import jp.co.highjunk.equipment.common.error.UnauthorizedException;

/**
 * A-03 ログイン中の人の情報の業務の処理（中身は AuthMeServiceImpl。詳細設計 auth/A-03_ログイン中の人の情報.md）。
 */
public interface AuthMeService {

	/**
	 * ログインしている人の情報と権限項目を返す。
	 *
	 * @throws UnauthorizedException ユーザが見つからない（削除された）とき（401）
	 */
	AuthMeRestOutDto me(AuthMeServiceInDto serviceInDto);
}
