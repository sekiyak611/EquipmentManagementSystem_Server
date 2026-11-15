package jp.co.highjunk.equipment.common.constant;

/**
 * パスワード設定用のリンクの用途（データ設計 5.8 の password_tokens.purpose。詳細設計 common.md 3.14）。
 * 値の文字は、データベースに入れる文字と同じ。
 */
public enum PasswordLinkPurpose {

	/** パスワードの再設定（A-04）。 */
	RESET,

	/** ユーザ登録のときの初めてのパスワード設定（G-03・G-06）。 */
	REGISTER,

	/** アカウントをロックしたときの、パスワードの設定し直し（A-01）。 */
	LOCK
}
