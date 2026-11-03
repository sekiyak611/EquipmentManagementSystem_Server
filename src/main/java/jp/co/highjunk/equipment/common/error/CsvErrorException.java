package jp.co.highjunk.equipment.common.error;

import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * CSVの行にエラーがある（400）。行ごとの理由を errors に入れる（ErrorItem.ofCsvRow）。
 */
public class CsvErrorException extends ApiException {

	public CsvErrorException(List<ErrorItem> rowErrors) {
		super(HttpStatus.BAD_REQUEST, ApiMessages.CSV_ERROR, rowErrors);
	}
}
