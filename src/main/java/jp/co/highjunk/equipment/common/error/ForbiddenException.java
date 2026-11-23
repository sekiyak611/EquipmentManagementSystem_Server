package jp.co.highjunk.equipment.common.error;

import org.springframework.http.HttpStatus;

/**
 * ほかの人の貸出・備品は操作できない（403）。例：ほかの人が借りている貸出履歴を返そうとした（B-03）、ほかの人の備品を確認しようとした（B-04）。
 * message には画面設計のメッセージを入れる。errors はなし（詳細設計 common.md 3.20）。
 */
public class ForbiddenException extends ApiException {

	public ForbiddenException(String message) {
		super(HttpStatus.FORBIDDEN, message, null);
	}
}
