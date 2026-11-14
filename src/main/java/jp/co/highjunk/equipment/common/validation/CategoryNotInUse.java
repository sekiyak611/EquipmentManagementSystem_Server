package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 種別を使っている備品（削除されていない備品。管理状態は問わない）がないかのチェック（機能一覧 4章 F-21、画面設計 16章）。
 * ServiceInDto の種別のID に付ける。
 * 使い方：{@code @CategoryNotInUse(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.BusinessRule.class)}（守れなければ 400）。
 * 備品は FOR SHARE で数えるので、先に {@code @CategoryExists(forUpdate = true)} で種別の行を押さえておく（K-04 の8章）。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryNotInUseCheck.class)
public @interface CategoryNotInUse {

	/** 使っている備品があって消せないときの文（画面設計 16章） */
	String MESSAGE = "この種別の備品があるため削除できません";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
