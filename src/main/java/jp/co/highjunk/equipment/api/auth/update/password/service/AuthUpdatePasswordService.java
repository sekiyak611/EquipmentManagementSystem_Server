package jp.co.highjunk.equipment.api.auth.update.password.service;

import jp.co.highjunk.equipment.common.error.FieldErrorException;

/**
 * A-07 自分のパスワード変更の業務の処理（中身は AuthUpdatePasswordServiceImpl。詳細設計 auth/A-07_自分のパスワード変更.md）。
 */
public interface AuthUpdatePasswordService {

	/**
	 * 現在のパスワードを確かめて、新しいパスワードにする（この端末以外のログインを切る）。変わったことをDM（④）で知らせる。
	 * 現在のパスワードを間違えたときは、回数を数える。
	 *
	 * @throws AuthUpdatePasswordWrongCurrentException    現在のパスワードが違うとき（400）
	 * @throws AuthUpdatePasswordAccountLockedException   現在のパスワードが違い、いまロックしたとき（400。Controller がこの端末をログアウトさせる）
	 * @throws FieldErrorException                        新しいパスワードが、現在と同じ・使えないものと同じとき（400）
	 */
	void update(AuthUpdatePasswordServiceInDto serviceInDto);
}
