package jp.co.highjunk.equipment.common.error;

import org.springframework.http.HttpStatus;

/**
 * ぶつかった（409）。例：ほかの人が先に保存した（楽観ロック）、すでに貸し出されている、すでに返却されている。
 * message には画面設計のメッセージを入れる。
 */
public class ConflictException extends ApiException {

	public ConflictException(String message) {
		super(HttpStatus.CONFLICT, message, null);
	}
}
