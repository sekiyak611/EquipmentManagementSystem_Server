package jp.co.highjunk.equipment.common.error;

import org.springframework.http.HttpStatus;

/**
 * 動いていない（503）。A-08で、データベースにつながらないとき。
 */
public class ServiceUnavailableException extends ApiException {

	public ServiceUnavailableException() {
		super(HttpStatus.SERVICE_UNAVAILABLE, ApiMessages.SERVICE_UNAVAILABLE, null);
	}
}
