package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 貸出履歴が、まだ返却されていない（返却日が空）かのチェック（詳細設計 common.md 3.20）。
 * ServiceInDto のクラスに付け、ServiceInDto は {@link Target} を実装する。
 * 使い方：{@code @LoanNotReturned(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.Conflict.class)}（守れなければ 409）。
 * 貸出履歴がない・取り消し済みのときは確かめない（{@link LoanExists} で先に確かめる）。
 */
@Documented
@java.lang.annotation.Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoanNotReturnedCheck.class)
public @interface LoanNotReturned {

	/** すでに返却されている貸出履歴を返そうとしたときの文（画面設計 3章） */
	String MESSAGE = "この備品はすでに返却されています";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/** このチェックを付ける ServiceInDto が持つ値。 */
	interface Target {

		/** 貸出履歴のID */
		Long getLoanId();
	}
}
