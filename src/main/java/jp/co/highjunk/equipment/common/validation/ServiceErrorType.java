package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.Payload;

/**
 * ServiceInDto のチェックで守れないときの返し方の印（詳細設計 common.md。CLAUDE.md 5章）。
 * チェック用アノテーションの payload に付ける。ServiceInDtoChecker が、印に合わせて common.error の例外に変える。
 * 何も付けなければ、入力欄のエラー（FieldErrorException。400。errors に入力欄ごとの文）になる。
 */
public final class ServiceErrorType {

	private ServiceErrorType() {
	}

	/** 見つからない（NotFoundException。404。文は「対象が見つかりません。」）。 */
	public interface NotFound extends Payload {
	}

	/** ぶつかった（ConflictException。409。文はチェック用アノテーションの message）。 */
	public interface Conflict extends Payload {
	}

	/** 決まりに合わない操作（BusinessRuleException。400。errors なし。文はチェック用アノテーションの message）。 */
	public interface BusinessRule extends Payload {
	}
}
