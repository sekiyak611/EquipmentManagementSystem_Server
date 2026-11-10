package jp.co.highjunk.equipment.common.error;

import org.springframework.http.HttpStatus;

/**
 * ログインできなかった・ログイン状態が読めなくなった（401）。
 * A-01 では、画面設計 2章の文「メールアドレスまたはパスワードが違います」を入れる（docs/API設計.md 4.1）。
 * ほかでは ApiMessages.UNAUTHORIZED を入れる。
 */
public class UnauthorizedException extends ApiException {

	public UnauthorizedException(String message) {
		super(HttpStatus.UNAUTHORIZED, message, null);
	}
}
