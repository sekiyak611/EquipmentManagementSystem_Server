package jp.co.highjunk.equipment.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * errors の1件（docs/API設計.md 2.4）。
 *
 * @param field        誤りのある入力欄の名前。CSVの行のエラーのときは null
 * @param row          CSVの行番号。CSVの行のエラーのときだけ入れる（null なら項目ごと出さない）
 * @param managementNo CSVのその行の管理番号。CSVの行のエラーのときだけ入れる（null なら項目ごと出さない）
 * @param message      入力欄ごと・行ごとのメッセージ（画面設計の文）
 */
public record ErrorItem(
		String field,
		@JsonInclude(JsonInclude.Include.NON_NULL) Integer row,
		@JsonInclude(JsonInclude.Include.NON_NULL) String managementNo,
		String message) {

	/** 入力欄のエラー。 */
	public static ErrorItem ofField(String field, String message) {
		return new ErrorItem(field, null, null, message);
	}

	/** CSVの行のエラー。 */
	public static ErrorItem ofCsvRow(int row, String managementNo, String message) {
		return new ErrorItem(null, row, managementNo, message);
	}
}
