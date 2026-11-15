package jp.co.highjunk.equipment.api.auth.passwordtokens.use.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.common.passwordlink.PasswordChangedNotifier;
import lombok.RequiredArgsConstructor;

/**
 * A-06 合言葉でパスワードを決める業務の処理の中身（詳細設計 auth/A-06_合言葉でパスワードを決める.md の4章）。
 * このメソッドには @Transactional を付けない。Slack の送信を、書き込みが確定したあとにするため
 * （書き込みは AuthPasswordTokensUsePasswordSetter が、自分のまとまりで行う）。
 */
@Service
@RequiredArgsConstructor
public class AuthPasswordTokensUseServiceImpl implements AuthPasswordTokensUseService {

	private static final Logger log = LoggerFactory.getLogger(AuthPasswordTokensUseServiceImpl.class);

	private final AuthPasswordTokensUsePasswordSetter passwordSetter;

	private final PasswordChangedNotifier notifier;

	@Override
	public void use(AuthPasswordTokensUseServiceInDto serviceInDto) {
		AuthPasswordTokensUsePasswordSetResult result = passwordSetter.set(serviceInDto.getToken(),
				serviceInDto.getPassword());
		log.info("パスワードを設定しました（userId={}、用途={}）", result.getUser().getId(), result.getPurpose());

		// 確定したあとで送る（Slack は @Transactional の外。CLAUDE.md 5章）。最初の設定（REGISTER）のときは送らない
		if (result.getPurpose() != PasswordLinkPurpose.REGISTER) {
			notifier.notifyChanged(result.getUser().getEmail(), result.getUser().getId(), result.getChangedAt());
		}
	}
}
