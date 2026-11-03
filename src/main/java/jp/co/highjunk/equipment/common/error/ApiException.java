package jp.co.highjunk.equipment.common.error;

import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * 各機能が「この番号とメッセージで返してほしい」ときに投げるエラーのもと。
 * 投げると、GlobalExceptionHandler が docs/API設計.md 2.4 の形に変えて返す。
 * 各機能では、この下の BusinessRuleException などを使う。
 */
public abstract class ApiException extends RuntimeException {

	private final HttpStatus status;

	private final transient List<ErrorItem> errors;

	protected ApiException(HttpStatus status, String message, List<ErrorItem> errors) {
		super(message);
		this.status = status;
		this.errors = errors == null ? null : List.copyOf(errors);
	}

	public HttpStatus getStatus() {
		return status;
	}

	/** errors に入れる一覧。入れないときは null。 */
	public List<ErrorItem> getErrors() {
		return errors;
	}
}
