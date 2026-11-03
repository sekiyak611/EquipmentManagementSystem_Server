package jp.co.highjunk.equipment.common.error;

import org.springframework.http.HttpStatus;

/**
 * 決まりに合わない操作（400）。例：貸出中の備品の削除、使われている種別の削除。
 * message には画面設計のメッセージを入れる。
 */
public class BusinessRuleException extends ApiException {

	public BusinessRuleException(String message) {
		super(HttpStatus.BAD_REQUEST, message, null);
	}
}
