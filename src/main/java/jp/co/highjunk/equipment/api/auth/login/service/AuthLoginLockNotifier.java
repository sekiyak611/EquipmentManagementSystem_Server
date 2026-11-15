package jp.co.highjunk.equipment.api.auth.login.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.config.AppProperties;
import jp.co.highjunk.equipment.common.passwordlink.IssuedPasswordLink;
import jp.co.highjunk.equipment.common.slack.SlackDirectMessage;
import jp.co.highjunk.equipment.common.slack.SlackSendResult;
import jp.co.highjunk.equipment.common.slack.SlackSender;
import jp.co.highjunk.equipment.dao.entity.User;
import lombok.RequiredArgsConstructor;

/**
 * A-01：ロックしたことを、本人にSlackのDM（画面設計 9章の③）で知らせる（詳細設計 auth/A-01_ログイン.md 4.1）。
 * {@code @Transactional} の外（書き込みが確定したあと）で呼ぶ（CLAUDE.md 5章）。送れなくても、ロックはそのまま。
 */
@Component
@RequiredArgsConstructor
public class AuthLoginLockNotifier {

	private static final Logger log = LoggerFactory.getLogger(AuthLoginLockNotifier.class);

	/** DMの見出し（画面設計 9章の③） */
	static final String TITLE = "【備品管理】アカウントをロックしました";

	private final SlackSender slackSender;

	private final AppProperties appProperties;

	/** ロックのDMを送る。送れなかったときは、ログに WARN で残すだけ（画面設計 9章の注意3）。 */
	public void notifyLocked(User user, IssuedPasswordLink link) {
		SlackSendResult result = slackSender.sendDirectMessage(user.getEmail(),
				new SlackDirectMessage(TITLE, body(link)));
		if (result != SlackSendResult.SENT) {
			log.warn("ロックのDMを送れませんでした（userId={}、結果={}）", user.getId(), result);
		}
	}

	/** 本文：ロックしたこと、リンクと有効期限、心当たりがないとき、期限が切れたとき（パスワードは書かない）。 */
	private String body(IssuedPasswordLink link) {
		long minutes = appProperties.passwordLink().lockValidity().toMinutes();
		return "パスワードを" + appProperties.lockThreshold() + "回続けて間違えたため、アカウントをロックしました。\n"
				+ "次のリンクから、パスワードを設定し直してください（有効期限：" + minutes + "分）。\n"
				+ link.getLinkUrl() + "\n\n"
				+ "ご自分で間違えた覚えがない場合は、だれかがログインを試した可能性があります。必ずパスワードを設定し直してください。\n"
				+ "リンクの期限が切れたときは、ログイン画面の「パスワードを忘れた方はこちら」から設定できます。";
	}
}
