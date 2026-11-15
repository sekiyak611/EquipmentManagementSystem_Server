package jp.co.highjunk.equipment.common.passwordlink;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.slack.SlackDirectMessage;
import jp.co.highjunk.equipment.common.slack.SlackSendResult;
import jp.co.highjunk.equipment.common.slack.SlackSender;
import lombok.RequiredArgsConstructor;

/**
 * パスワードが変わったことを、本人にSlackのDM（画面設計 9章の④）で知らせる（詳細設計 common.md 3.15。A-06・A-07 が使う）。
 * パスワードは書かない。{@code @Transactional} の外（書き込みが確定したあと）で呼ぶ（CLAUDE.md 5章）。
 * 送れなくても、パスワードの変更はそのまま。ログに WARN で残すだけ（画面設計 9章の注意3）。
 */
@Component
@RequiredArgsConstructor
public class PasswordChangedNotifier {

	private static final Logger log = LoggerFactory.getLogger(PasswordChangedNotifier.class);

	/** DMの見出し（画面設計 9章の④） */
	static final String TITLE = "【備品管理】パスワードを変更しました";

	private static final DateTimeFormatter CHANGED_AT = DateTimeFormatter.ofPattern("yyyy年M月d日 H:mm", Locale.JAPAN);

	private final SlackSender slackSender;

	/**
	 * DMを送る。
	 *
	 * @param email     宛先（ユーザに登録したメールアドレス）
	 * @param userId    ログに残すユーザのID
	 * @param changedAt パスワードが変わった日時（日本時間。Clock から取ったもの）
	 */
	public void notifyChanged(String email, long userId, LocalDateTime changedAt) {
		SlackSendResult result = slackSender.sendDirectMessage(email,
				new SlackDirectMessage(TITLE, body(changedAt)));
		if (result != SlackSendResult.SENT) {
			log.warn("パスワードを変更したことのDMを送れませんでした（userId={}、結果={}）", userId, result);
		}
	}

	/** 本文：変わった日時と、心当たりがないときのこと（パスワードは書かない）。 */
	private String body(LocalDateTime changedAt) {
		return "パスワードを変更しました。\n"
				+ "変更した日時：" + CHANGED_AT.format(changedAt) + "\n\n"
				+ "ご自分で変更した覚えがない場合は、すぐにパスワードを再設定し、総務に連絡してください。";
	}
}
