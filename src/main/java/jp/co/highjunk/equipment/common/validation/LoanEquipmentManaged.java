package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 貸出履歴の備品が、管理中（MANAGED）かのチェック（管理中でない備品は返せない。詳細設計 common.md 3.20）。
 * ServiceInDto のクラスに付け、ServiceInDto は {@link Target} を実装する。
 * 使い方：{@code @LoanEquipmentManaged(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.BusinessRule.class)}（守れなければ 400）。
 * 備品の行を FOR UPDATE で押さえる（貸出履歴 → 備品の順。{@link LoanExists} が貸出履歴を先に押さえる）。
 * 備品が見つからない（削除済み）ときも、管理中でないものとして、守れない扱いにする。
 * 貸出履歴がない・取り消し済みのときは確かめない（{@link LoanExists} で先に確かめる）。
 */
@Documented
@java.lang.annotation.Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoanEquipmentManagedCheck.class)
public @interface LoanEquipmentManaged {

	/** 管理中でなくなった備品を返そうとしたときの文（B-03。2026-11-23 に人が決めた言葉をもとにした。言い方は人の確認がまだ） */
	String MESSAGE = "この備品は返却できません。総務へお問い合わせください。";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/** このチェックを付ける ServiceInDto が持つ値。 */
	interface Target {

		/** 貸出履歴のID */
		Long getLoanId();
	}
}
