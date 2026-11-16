package jp.co.highjunk.equipment.api.auth.passwordtokens.use.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.api.auth.passwordtokens.use.validator.AuthPasswordTokensUseValidator;
import jp.co.highjunk.equipment.api.auth.passwordtokens.use.validator.AuthPasswordTokensUseValidatorDto;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkVerifier;
import jp.co.highjunk.equipment.common.passwordlink.VerifiedPasswordLink;
import jp.co.highjunk.equipment.common.security.LoginRememberMe;
import jp.co.highjunk.equipment.dao.entity.PasswordToken;
import jp.co.highjunk.equipment.dao.entity.PasswordTokenExample;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.mapper.PasswordTokenMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.UserCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * A-06：合言葉を確かめ、リンクを使用済みにして、パスワードを変える（詳細設計 auth/A-06_合言葉でパスワードを決める.md の4章の4）。
 * ServiceImpl から分けた理由：書き換えが2つ（リンク・ユーザ）あるので1つのまとまりにしつつ、Slack の送信を、
 * このまとまりが確定したあとにするため（CLAUDE.md 5章）。
 */
@Component
@RequiredArgsConstructor
public class AuthPasswordTokensUsePasswordSetter {

	private static final Logger log = LoggerFactory.getLogger(AuthPasswordTokensUsePasswordSetter.class);

	private final PasswordLinkVerifier verifier;

	private final AuthPasswordTokensUseValidator validator;

	private final PasswordTokenMapper passwordTokenMapper;

	private final UserCustomMapper userCustomMapper;

	private final PasswordEncoder passwordEncoder;

	private final LoginRememberMe loginRememberMe;

	private final Clock clock;

	/**
	 * パスワードを決める。確かめの順番：合言葉 → パスワード（Validator）→ リンクを使用済みに → パスワードを変える。
	 * パスワードで断られたときは、リンクを使用済みにしない（同じリンクで、もう一度決められる）。
	 *
	 * @throws BusinessRuleException 合言葉が使えないとき・同時にほかで使われたとき（400）
	 */
	@Transactional
	public AuthPasswordTokensUsePasswordSetResult set(String token, String password) {
		VerifiedPasswordLink link = verifier.verify(token);
		User user = link.getUser();
		validator.validate(new AuthPasswordTokensUseValidatorDto(user.getEmail(), password));

		LocalDateTime now = LocalDateTime.now(clock);

		// 使用済みにする。まだ使っていない・無効でない・期限内の行だけを書き換えるので、同時に2回使われても、成功するのは1つだけ
		PasswordToken used = new PasswordToken();
		used.setUsedAt(now);
		PasswordTokenExample example = new PasswordTokenExample();
		example.createCriteria().andIdEqualTo(link.getPasswordToken().getId()).andUsedAtIsNull()
				.andRevokedAtIsNull().andExpiresAtGreaterThan(now);
		if (passwordTokenMapper.updateByExampleSelective(used, example) != 1) {
			throw unusable("同時にほかで使われた", user.getId());
		}

		// session_valid_after は秒までにそろえて入れる（A-06 の12章 No.1）
		if (userCustomMapper.updatePassword(user.getId(), passwordEncoder.encode(password),
				now.truncatedTo(ChronoUnit.SECONDS), now) != 1) {
			throw unusable("ユーザが削除された", user.getId());
		}
		// すべての端末の「ログインしたまま」を切る（common.md 3.17の6）。まとまりの中なので、元に戻るときは一緒に戻る
		loginRememberMe.removeAllOf(user.getEmail());
		return new AuthPasswordTokensUsePasswordSetResult(user, link.getPurpose(), now);
	}

	/** 理由をログに残して、同じ文の例外を作る。まとまり全体が元に戻る。 */
	private BusinessRuleException unusable(String reason, long userId) {
		log.warn("パスワード設定用のリンクが使えません（理由={}、userId={}）", reason, userId);
		return new BusinessRuleException(PasswordLinkVerifier.UNUSABLE_MESSAGE);
	}
}
