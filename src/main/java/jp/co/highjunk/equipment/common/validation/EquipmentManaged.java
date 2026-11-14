package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 備品の管理状態が管理中（MANAGED）かのチェック（画面設計 10章。C-06 で、管理中の備品だけ確認できる）。
 * ServiceInDto の備品のID に付ける。
 * 使い方：{@code @EquipmentManaged(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.BusinessRule.class)}（守れなければ 400）。
 * 備品がない・削除済みのときは確かめない（{@link EquipmentExists} で先に確かめる）。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EquipmentManagedCheck.class)
public @interface EquipmentManaged {

	/** 管理中でない備品を確認しようとしたときの文（画面設計 10章。2026-11-11 に人が決めた） */
	String MESSAGE = "管理中の備品だけ確認できます";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
