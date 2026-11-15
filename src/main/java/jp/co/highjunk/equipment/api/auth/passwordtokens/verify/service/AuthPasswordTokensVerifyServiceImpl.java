package jp.co.highjunk.equipment.api.auth.passwordtokens.verify.service;

import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.api.auth.passwordtokens.verify.model.AuthPasswordTokensVerifyRestOutDto;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkVerifier;
import jp.co.highjunk.equipment.common.passwordlink.VerifiedPasswordLink;
import lombok.RequiredArgsConstructor;

/**
 * A-05 合言葉の確認の業務の処理の中身（詳細設計 auth/A-05_合言葉の確認.md の4章）。
 * 読むだけなので @Transactional は付けない。リンクを開いただけでは使用済みにしない（画面設計 9章の注意2）。
 * 確かめは、A-06 と同じ common.passwordlink.PasswordLinkVerifier で行う（API どうしは使い合わないため）。
 */
@Service
@RequiredArgsConstructor
public class AuthPasswordTokensVerifyServiceImpl implements AuthPasswordTokensVerifyService {

	private final PasswordLinkVerifier verifier;

	@Override
	public AuthPasswordTokensVerifyRestOutDto verify(AuthPasswordTokensVerifyServiceInDto serviceInDto) {
		VerifiedPasswordLink link = verifier.verify(serviceInDto.getToken());
		return new AuthPasswordTokensVerifyRestOutDto(link.getUser().getEmail(), link.getPurpose().name());
	}
}
