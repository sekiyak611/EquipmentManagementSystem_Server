package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 種別の名前が、削除されていないほかの種別と重ならないかのチェック（画面設計 16章）。守れなければ、入力欄 name のエラー（400）。
 * ServiceInDto のクラスに付け、ServiceInDto は {@link Target} を実装する。payload は付けない。
 * 名前は、照合順序 utf8mb4_ja_0900_as_cs_ks で比べる（データ設計 2章。SQL の = で比べる）。
 */
@Documented
@java.lang.annotation.Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryNameNotDuplicatedCheck.class)
public @interface CategoryNameNotDuplicated {

	/** 名前が重なるときの文（画面設計 16章） */
	String MESSAGE = "この名前はすでに使われています";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/** このチェックを付ける ServiceInDto が持つ値。 */
	interface Target {

		/** 数えない種別のID（K-03 で、変える種別自身）。足すとき（K-02）は null */
		Long getId();

		/** 名前（前後の空白を取り除いたもの） */
		String getName();
	}
}
