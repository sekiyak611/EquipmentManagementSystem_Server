package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 種別の頭文字が、削除されていないほかの種別と重ならないかのチェック（機能一覧 4章 F-21、画面設計 16章）。
 * 守れなければ、入力欄 initial のエラー（400）。頭文字がない（null）ときは確かめない。
 * ServiceInDto のクラスに付け、ServiceInDto は {@link Target} を実装する。payload は付けない。
 */
@Documented
@java.lang.annotation.Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryInitialNotDuplicatedCheck.class)
public @interface CategoryInitialNotDuplicated {

	/** 頭文字が重なるときの文（画面設計 16章） */
	String MESSAGE = "この頭文字はすでに使われています";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/** このチェックを付ける ServiceInDto が持つ値。 */
	interface Target {

		/** 数えない種別のID（K-03 で、変える種別自身）。足すとき（K-02）は null */
		Long getId();

		/** 頭文字（大文字に直したもの）。ないときは null */
		String getInitial();
	}
}
