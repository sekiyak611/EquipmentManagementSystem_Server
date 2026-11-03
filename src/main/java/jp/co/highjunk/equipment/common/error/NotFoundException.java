package jp.co.highjunk.equipment.common.error;

import org.springframework.http.HttpStatus;

/**
 * 見つからない（404）。例：削除された備品。
 */
public class NotFoundException extends ApiException {

	public NotFoundException() {
		super(HttpStatus.NOT_FOUND, ApiMessages.NOT_FOUND, null);
	}
}
