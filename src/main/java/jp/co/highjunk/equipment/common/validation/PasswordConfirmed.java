package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 新しいパスワードと、その確認が同じかのチェック（画面設計 6章・詳細設計 common.md 3.15）。
 * RestInDto のクラスに付け、RestInDto は {@link PasswordConfirmable} を実装する（A-06・A-07）。
 * 違うとき（確認が空・ないときも）、入力欄 passwordConfirm のエラーにする。
 */
@Documented
@java.lang.annotation.Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordConfirmedCheck.class)
public @interface PasswordConfirmed {

	/** 違うときの文（画面設計 6章） */
	String MESSAGE = "確認用のパスワードが一致しません";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
