package jp.co.highjunk.equipment.api.auth.passwordtokens.use.service;

import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.common.error.FieldErrorException;

/**
 * A-06 合言葉でパスワードを決める業務の処理（中身は AuthPasswordTokensUseServiceImpl。
 * 詳細設計 auth/A-06_合言葉でパスワードを決める.md）。
 */
public interface AuthPasswordTokensUseService {

	/**
	 * 合言葉を使って、新しいパスワードを決める（リンクを使用済みにし、ロックを解き、すべての端末のログインを切る）。
	 * 用途が REGISTER 以外のときは、変わったことをDM（④）で知らせる。
	 *
	 * @throws BusinessRuleException 合言葉が使えないとき（400）
	 * @throws FieldErrorException   パスワードが、メールアドレス・システム名・よく使われるパスワードと同じとき（400）
	 */
	void use(AuthPasswordTokensUseServiceInDto serviceInDto);
}
