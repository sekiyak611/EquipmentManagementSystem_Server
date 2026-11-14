package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 種別の version が、画面を開いたときと同じか（ほかの人が先に保存していないか）のチェック（楽観ロック。API設計 2.7）。
 * ServiceInDto のクラスに付け、ServiceInDto は {@link Target} を実装する。
 * 使い方：{@code @CategoryVersionMatches(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.Conflict.class)}（守れなければ 409）。
 * 種別がない・削除済みのときは確かめない（{@link CategoryExists} で先に確かめる）。
 */
@Documented
@java.lang.annotation.Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryVersionMatchesCheck.class)
public @interface CategoryVersionMatches {

	/** ほかの人が先に保存していたときの文（画面設計 16章） */
	String MESSAGE = "ほかの人が先にこの種別を更新しました。画面を開き直して、最新の内容を確かめてください。";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/** このチェックを付ける ServiceInDto が持つ値。 */
	interface Target {

		/** 種別のID */
		Long getId();

		/** 画面を開いたときに受け取った version */
		Integer getVersion();
	}
}
