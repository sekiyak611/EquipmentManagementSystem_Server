package jp.co.highjunk.equipment.common.error;

import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * APIが返す値の形。正常時もエラー時も、この形にそろえる（docs/API設計.md 2.4）。
 *
 * <pre>
 * { "status": 200, "message": "処理が正常に完了しました。", "data": {...}, "errors": null }
 * </pre>
 *
 * @param status  HTTPステータスと同じ番号
 * @param message 結果を表す文
 * @param data    正常時に返す中身。返すものがないとき・エラー時は null
 * @param errors  入力欄ごと・CSVの行ごとのエラー。入力の誤り・CSVの行のエラーのときだけ入れ、ほかは null
 * @param <T>     data の型
 */
public record ApiResponse<T>(int status, String message, T data, List<ErrorItem> errors) {

	/** 成功（200）。返すものがないときは data に null を渡す。 */
	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(HttpStatus.OK.value(), ApiMessages.SUCCESS, data, null);
	}

	/** 作った（201）。 */
	public static <T> ApiResponse<T> created(T data) {
		return new ApiResponse<>(HttpStatus.CREATED.value(), ApiMessages.SUCCESS, data, null);
	}

	/** 受け付けた（202）。 */
	public static <T> ApiResponse<T> accepted() {
		return new ApiResponse<>(HttpStatus.ACCEPTED.value(), ApiMessages.SUCCESS, null, null);
	}

	/** エラー。data はいつも null。 */
	public static ApiResponse<Void> error(HttpStatus status, String message, List<ErrorItem> errors) {
		return new ApiResponse<>(status.value(), message, null, errors);
	}
}
