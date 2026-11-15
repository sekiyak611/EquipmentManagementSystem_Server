package jp.co.highjunk.equipment.api.auth.passwordresetrequests.service;

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
 * A-04：パスワード設定用のリンクを、本人にSlackのDM（画面設計 9章の①）で送る（詳細設計 auth/A-04_パスワード設定リンクの依頼.md の11章）。
 * {@code @Transactional} の外（書き込みが確定したあと）で呼ぶ（CLAUDE.md 5章）。
 */
@Component
@RequiredArgsConstructor
public class AuthPasswordResetRequestsNotifier {

	private static final Logger log = LoggerFactory.getLogger(AuthPasswordResetRequestsNotifier.class);

	/** DMの見出し（画面設計 9章の①） */
	static final String TITLE = "【備品管理】パスワード再設定のご案内";

	private final SlackSender slackSender;

	private final AppProperties appProperties;

	/** DMを送る。送れなかったときは、ログに WARN で残すだけ（画面設計 5章・9章の注意3）。 */
	public void notifyRequested(User user, IssuedPasswordLink link) {
		SlackSendResult result = slackSender.sendDirectMessage(user.getEmail(),
				new SlackDirectMessage(TITLE, body(link)));
		if (result != SlackSendResult.SENT) {
			log.warn("パスワード設定用のリンクのDMを送れませんでした（userId={}、結果={}）", user.getId(), result);
		}
	}

	/** 本文：リンクと有効期限、心当たりがないとき（パスワードは書かない）。 */
	private String body(IssuedPasswordLink link) {
		long minutes = appProperties.passwordLink().resetValidity().toMinutes();
		return "パスワードの再設定の依頼を受け付けました。\n"
				+ "次のリンクから、新しいパスワードを設定してください（有効期限：" + minutes + "分）。\n"
				+ link.getLinkUrl() + "\n\n"
				+ "依頼した覚えがない場合は、このDMは無視してください。パスワードは変わりません。";
	}
}
