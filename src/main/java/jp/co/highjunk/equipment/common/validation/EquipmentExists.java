package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 備品のID が、削除されていない備品を指しているかのチェック（ServiceInDto の備品のID に付ける。CLAUDE.md 5章）。
 * 使い方：{@code @EquipmentExists(groups = ServiceCheckOrder.First.class, payload = ServiceErrorType.NotFound.class)}（守れなければ 404）。
 * null は確かめない（守れている扱い）。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EquipmentExistsCheck.class)
public @interface EquipmentExists {

	String message() default "対象が見つかりません。";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * true なら、読んだ備品の行を、まとまり（トランザクション）が終わるまで押さえる（FOR UPDATE）。
	 * C-06 で、確かめてから書き換えるまでの間に、ほかの人に変えられないようにするため。
	 */
	boolean forUpdate() default false;
}
