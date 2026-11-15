package jp.co.highjunk.equipment.common.slack;

/**
 * SlackのDMを送った結果（詳細設計 common.md 3.13）。送る側は、この値を見て、画面設計どおりに分ける。
 */
public enum SlackSendResult {

	/** 送れた。 */
	SENT,

	/** そのメールアドレスの Slack のユーザーが見つからない。 */
	USER_NOT_FOUND,

	/** そのほか（トークンが未設定、Slack が断った、通信の失敗）。 */
	FAILED
}
