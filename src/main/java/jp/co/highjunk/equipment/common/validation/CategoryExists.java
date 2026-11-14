package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 種別のID が、削除されていない種別を指しているかのチェック（ServiceInDto の種別のID に付ける。CLAUDE.md 5章）。
 * 使い方：{@code @CategoryExists(groups = ServiceCheckOrder.First.class, payload = ServiceErrorType.NotFound.class)}（守れなければ 404）。
 * null は確かめない（守れている扱い）。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryExistsCheck.class)
public @interface CategoryExists {

	String message() default "対象が見つかりません。";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * true なら、読んだ種別の行を、まとまり（トランザクション）が終わるまで押さえる（FOR UPDATE）。
	 * K-04 で、確かめから消すまでの間に、その種別の備品を足されないようにするため。
	 */
	boolean forUpdate() default false;
}
