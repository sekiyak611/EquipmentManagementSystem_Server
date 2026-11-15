package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 新しいパスワードの形式のチェック（画面設計 8章。詳細設計 common.md 3.15）。RestInDto の password に付ける（A-06・A-07）。
 * 1つの欄に1つだけメッセージを出す。順番は、空 → 使えない文字 → 短い → 長い。
 * メールアドレス・システム名・よく使われるパスワードとの照らし合わせは、読んだユーザの値が要るので、ここではしない（Validator で行う）。
 */
@Documented
@java.lang.annotation.Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NewPasswordFormatCheck.class)
public @interface NewPasswordFormat {

	/** 空のときの文（画面設計 8章） */
	String EMPTY = "新しいパスワードを入力してください";

	/** 使えない文字があるときの文（画面設計 8章） */
	String BAD_CHARACTER = "パスワードに使えるのは、半角の英字・数字・記号・スペースです";

	/** 短いときの文（画面設計 8章） */
	String TOO_SHORT = "パスワードは8文字以上で入力してください";

	/** 長いときの文（画面設計 8章） */
	String TOO_LONG = "パスワードは64文字以内で入力してください";

	/** 最小の長さ（画面設計 8章） */
	int MIN_LENGTH = 8;

	/** 最大の長さ（画面設計 8章） */
	int MAX_LENGTH = 64;

	String message() default EMPTY;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
