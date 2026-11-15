package jp.co.highjunk.equipment.api.auth.passwordresetrequests.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.config.AppProperties;
import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.common.passwordlink.IssuedPasswordLink;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkIssuer;
import jp.co.highjunk.equipment.dao.entity.PasswordTokenExample;
import jp.co.highjunk.equipment.dao.mapper.PasswordTokenMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.UserCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * A-04：5分以内の依頼でなければ、パスワード設定用のリンクを作る（詳細設計 auth/A-04_パスワード設定リンクの依頼.md の4章の5）。
 * ServiceImpl から分けた理由：確かめとリンクを作る書き込み（前のリンクを無効にする・新しいリンクを入れる）を1つの
 * まとまりにしつつ、Slack の送信は、このまとまりが確定したあとにするため（CLAUDE.md 5章）。
 */
@Component
@RequiredArgsConstructor
public class AuthPasswordResetRequestsLinkIssuer {

	private static final Logger log = LoggerFactory.getLogger(AuthPasswordResetRequestsLinkIssuer.class);

	private final UserCustomMapper userCustomMapper;

	private final PasswordTokenMapper passwordTokenMapper;

	private final PasswordLinkIssuer passwordLinkIssuer;

	private final AppProperties appProperties;

	private final Clock clock;

	/**
	 * その人の、5分以内の再設定のリンクがなければ、リンク（用途 RESET）を作る。
	 * 先にユーザの行をロックするので、同じ人への依頼が同時に来ても、1つずつ処理される（あとの依頼は、先の依頼のリンクを見て断る）。
	 *
	 * @return 作ったリンク。5分以内の依頼があったときは空
	 */
	@Transactional
	public Optional<IssuedPasswordLink> issueIfAllowed(long userId) {
		userCustomMapper.selectIdForUpdate(userId);

		LocalDateTime threshold = LocalDateTime.now(clock).minus(appProperties.passwordLink().requestInterval());
		PasswordTokenExample example = new PasswordTokenExample();
		example.createCriteria().andUserIdEqualTo(userId).andPurposeEqualTo(PasswordLinkPurpose.RESET.name())
				.andCreatedAtGreaterThan(threshold);
		if (passwordTokenMapper.countByExample(example) > 0) {
			log.info("パスワード再設定の依頼を受け付けましたが、前の依頼から間もない（app.password-link.request-interval 以内の）ので、リンクは作りません（userId={}）", userId);
			return Optional.empty();
		}
		return Optional.of(passwordLinkIssuer.issue(userId, PasswordLinkPurpose.RESET));
	}
}
