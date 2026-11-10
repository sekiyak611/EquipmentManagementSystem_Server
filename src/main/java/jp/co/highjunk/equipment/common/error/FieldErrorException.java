package jp.co.highjunk.equipment.common.error;

import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * 業務チェック（データベースの中身と照らした結果）を、入力欄のエラーとして返すときの例外（400。詳細設計 common.md 6章）。
 * 例：種別の名前が、ほかの種別ともう重なっている（画面設計 16章「この名前はすでに使われています」）。
 * message は入力の誤りと同じ「入力内容に誤りがあります。」、errors に入力欄ごとの文を入れる（API設計 2.4）。
 * 受け取った値だけで分かる入力チェックは、これを使わず dto の印（@NotBlank など）で行う（CLAUDE.md 5章）。
 */
public class FieldErrorException extends ApiException {

	/** 入力欄1つのエラー。 */
	public FieldErrorException(String field, String message) {
		this(List.of(ErrorItem.ofField(field, message)));
	}

	/** 入力欄のエラーをまとめて返す（2つ以上の入力欄で同時に重なったときなど）。fieldErrors は1件以上。 */
	public FieldErrorException(List<ErrorItem> fieldErrors) {
		super(HttpStatus.BAD_REQUEST, ApiMessages.VALIDATION_ERROR, fieldErrors);
	}
}
