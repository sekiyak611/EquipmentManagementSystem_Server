package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 貸出履歴の借りた人が、ログインしている人かのチェック（ほかの人の貸出履歴は操作できない。IDOR を防ぐ。詳細設計 common.md 3.20）。
 * ServiceInDto のクラスに付け、ServiceInDto は {@link Target} を実装する。
 * 使い方：{@code @LoanBorrowedBySelf(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.Forbidden.class)}（守れなければ 403）。
 * 貸出履歴がない・取り消し済みのときは確かめない（{@link LoanExists} で先に確かめる）。
 */
@Documented
@java.lang.annotation.Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoanBorrowedBySelfCheck.class)
public @interface LoanBorrowedBySelf {

	/** ほかの人が借りている貸出履歴を返そうとしたときの文（B-03。2026-11-23 に人が決めた） */
	String MESSAGE = "ほかの人が借りているため、返却はできません。";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/** このチェックを付ける ServiceInDto が持つ値。 */
	interface Target {

		/** 貸出履歴のID */
		Long getLoanId();

		/** ログインしている人のユーザID */
		Long getLoginUserId();
	}
}
