package jp.co.highjunk.equipment.common.error;

/**
 * 返す値の message に入れる、決まった文（docs/API設計.md 2.4）。
 * 決まりに合わない操作（400）と、ぶつかったとき（409）の文は、画面設計の文を各機能で使う。
 */
public final class ApiMessages {

	/** 成功（200・201・202） */
	public static final String SUCCESS = "処理が正常に完了しました。";

	/** 入力の誤り（400） */
	public static final String VALIDATION_ERROR = "入力内容に誤りがあります。";

	/** CSVの行にエラーがある（400） */
	public static final String CSV_ERROR = "エラーがあったため、更新していません。直してからもう一度アップロードしてください。";

	/** ログインしていない（401） */
	public static final String UNAUTHORIZED = "ログインしてください。";

	/** 権限がない（403） */
	public static final String FORBIDDEN = "この操作を行う権限がありません。";

	/** 見つからない（404） */
	public static final String NOT_FOUND = "対象が見つかりません。";

	/**
	 * 上のどれにも当たらない、送り方の誤り（405：使えない操作の種類、415：使えない形のデータ など）。
	 * docs/API設計.md には書いていない場面のための文。
	 */
	public static final String BAD_REQUEST = "リクエストの形が正しくありません。";

	/** サーバーの不具合（500） */
	public static final String INTERNAL_ERROR = "問題が発生しました。時間をおいて、もう一度お試しください。続く場合は総務に連絡してください。";

	/** 動いていない（503。A-08） */
	public static final String SERVICE_UNAVAILABLE = "現在利用できません。";

	private ApiMessages() {
	}
}
