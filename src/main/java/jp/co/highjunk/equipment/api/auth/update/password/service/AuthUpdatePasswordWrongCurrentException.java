package jp.co.highjunk.equipment.api.auth.update.password.service;

import jp.co.highjunk.equipment.common.error.FieldErrorException;

/**
 * 現在のパスワードが違う（A-07。400。入力欄 currentPassword のエラー）。画面設計 7章の文を入れる。
 * 401 にしないのは、画面が「ログインの期限が切れた」と受け取って、ログイン画面に移してしまうため（詳細設計 A-07 の12章 No.2）。
 */
public class AuthUpdatePasswordWrongCurrentException extends FieldErrorException {

	/** 現在のパスワードが違うときの文（画面設計 7章） */
	public static final String MESSAGE = "現在のパスワードが違います";

	public AuthUpdatePasswordWrongCurrentException() {
		super("currentPassword", MESSAGE);
	}
}
