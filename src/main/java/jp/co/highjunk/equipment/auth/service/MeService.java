package jp.co.highjunk.equipment.auth.service;

import jp.co.highjunk.equipment.auth.model.User;
import jp.co.highjunk.equipment.common.error.UnauthorizedException;
import jp.co.highjunk.equipment.dto.auth.MeResponse;

/**
 * A-03 ログイン中の人の情報の業務のルール（中身は MeServiceImpl。詳細設計 auth/A-03_ログイン中の人の情報.md）。
 */
public interface MeService {

	/**
	 * ログインしている人の情報と権限項目を返す。
	 *
	 * @throws UnauthorizedException ユーザが見つからない（削除された）とき（401）
	 */
	MeResponse me(long userId);

	/**
	 * 読んだユーザから、A-03 の形の値を作る。A-01（LoginServiceImpl）も、ログインできた人をこの形で返すために使う。
	 */
	MeResponse toMeResponse(User user);
}
