package jp.co.highjunk.equipment.common.passwordlink;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.config.AppProperties;
import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.dao.entity.PasswordToken;
import jp.co.highjunk.equipment.dao.entity.PasswordTokenExample;
import jp.co.highjunk.equipment.dao.mapper.PasswordTokenMapper;
import lombok.RequiredArgsConstructor;

/**
 * パスワード設定用のリンクを作る（詳細設計 common.md 3.14）。
 * 合言葉を作り、ハッシュだけを保存し、その人の前のリンクを無効にして、リンクのアドレスを返す。
 * 自分では {@code @Transactional} を付けない。呼ぶ側のまとまりの中で動く（書き込みが2つあるので、呼ぶ側が付ける）。
 */
@Component
@RequiredArgsConstructor
public class PasswordLinkIssuer {

	private static final Logger log = LoggerFactory.getLogger(PasswordLinkIssuer.class);

	private final PasswordTokenMapper passwordTokenMapper;

	private final AppProperties appProperties;

	private final Clock clock;

	/**
	 * リンクを作る。
	 *
	 * @param userId  リンクを送る人
	 * @param purpose 用途（有効期限が用途で変わる）
	 */
	public IssuedPasswordLink issue(long userId, PasswordLinkPurpose purpose) {
		LocalDateTime now = LocalDateTime.now(clock);

		// 前のリンクを無効にする（まだ使っていない・まだ無効にしていないもの。用途が違っても無効にする）
		PasswordTokenExample example = new PasswordTokenExample();
		example.createCriteria().andUserIdEqualTo(userId).andUsedAtIsNull().andRevokedAtIsNull();
		PasswordToken revoked = new PasswordToken();
		revoked.setRevokedAt(now);
		passwordTokenMapper.updateByExampleSelective(revoked, example);

		String token = PasswordLinkTokens.generate();
		LocalDateTime expiresAt = now.plus(validity(purpose));

		PasswordToken row = new PasswordToken();
		row.setUserId(userId);
		row.setTokenHash(PasswordLinkTokens.hash(token));
		row.setPurpose(purpose.name());
		row.setExpiresAt(expiresAt);
		row.setCreatedAt(now);
		passwordTokenMapper.insert(row);

		// 合言葉・アドレスは出さない（API設計 7章）
		log.info("パスワード設定用のリンクを作りました（userId={}、用途={}）", userId, purpose);
		return new IssuedPasswordLink(linkUrl(token), purpose, expiresAt);
	}

	private Duration validity(PasswordLinkPurpose purpose) {
		AppProperties.PasswordLink setting = appProperties.passwordLink();
		return switch (purpose) {
		case RESET -> setting.resetValidity();
		case LOCK -> setting.lockValidity();
		case REGISTER -> setting.registrationValidity();
		};
	}

	/** `<app.base-url>/password-set?token=<合言葉>`。base-url の終わりの「/」は除く。 */
	private String linkUrl(String token) {
		String base = appProperties.baseUrl();
		while (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/password-set?token=" + token;
	}
}
