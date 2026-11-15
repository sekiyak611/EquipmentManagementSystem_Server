package jp.co.highjunk.equipment.api.auth.passwordtokens.verify.service;

import jp.co.highjunk.equipment.api.auth.passwordtokens.verify.model.AuthPasswordTokensVerifyRestOutDto;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;

/**
 * A-05 合言葉の確認の業務の処理（中身は AuthPasswordTokensVerifyServiceImpl。詳細設計 auth/A-05_合言葉の確認.md）。
 */
public interface AuthPasswordTokensVerifyService {

	/**
	 * 合言葉が使えるかを確かめ、対象のメールアドレスと用途を返す。何も書き換えない（使用済みにしない）。
	 *
	 * @throws BusinessRuleException 使えないとき（400）
	 */
	AuthPasswordTokensVerifyRestOutDto verify(AuthPasswordTokensVerifyServiceInDto serviceInDto);
}
