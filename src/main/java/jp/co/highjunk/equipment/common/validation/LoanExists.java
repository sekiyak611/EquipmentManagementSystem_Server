package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 貸出履歴のID が、取り消されていない貸出履歴を指しているかのチェック（ServiceInDto の貸出履歴のID に付ける。詳細設計 common.md 3.20）。
 * 使い方：{@code @LoanExists(forUpdate = true, groups = ServiceCheckOrder.First.class, payload = ServiceErrorType.NotFound.class)}（守れなければ 404）。
 * null は確かめない（守れている扱い）。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoanExistsCheck.class)
public @interface LoanExists {

	String message() default "対象が見つかりません。";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * true なら、読んだ貸出履歴の行を、まとまり（トランザクション）が終わるまで押さえる（FOR UPDATE）。
	 * 確かめてから書き換えるまでの間に、ほかの人に変えられないようにするため（B-03）。
	 */
	boolean forUpdate() default false;
}
