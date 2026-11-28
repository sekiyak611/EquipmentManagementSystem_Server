package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 権限のID が、削除されていない権限を指しているかのチェック（ServiceInDto の権限のID に付ける。CLAUDE.md 5章）。
 * 使い方：{@code @RoleExists(groups = ServiceCheckOrder.First.class, payload = ServiceErrorType.NotFound.class)}（守れなければ 404）。
 * null は確かめない（守れている扱い）。読むだけで、行は押さえない（H-04・H-05 で押さえる必要が出たら、forUpdate などを足す）。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RoleExistsCheck.class)
public @interface RoleExists {

	String message() default "対象が見つかりません。";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
