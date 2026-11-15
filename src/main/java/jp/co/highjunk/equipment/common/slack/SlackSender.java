package jp.co.highjunk.equipment.common.slack;

/**
 * ユーザに登録したメールアドレスで Slack のユーザーを探し、アプリからDMを送る約束（詳細設計 common.md 3.13）。
 * 例外を投げない。送れなかったときは、結果の値で知らせる。
 * 呼ぶときは {@code @Transactional} の中で呼ばず、書き込みが確定してから呼ぶ（CLAUDE.md 5章）。
 */
public interface SlackSender {

	/**
	 * DMを送る。
	 *
	 * @param email   宛先（ユーザに登録したメールアドレス。Slack に登録しているものと同じ）
	 * @param message 送るDM
	 * @return 送った結果
	 */
	SlackSendResult sendDirectMessage(String email, SlackDirectMessage message);
}
