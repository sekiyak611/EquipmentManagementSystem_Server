package jp.co.highjunk.equipment.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 備品の管理番号が、削除されていないほかの備品と重ならないかのチェック（画面設計 12章）。削除済みの備品の番号は使い直せる。
 * ServiceInDto の管理番号に付ける。payload は付けない（守れなければ、入力欄 managementNo のエラー 400）。null は確かめない。
 * 備品を変える API（C-04）で自分を除く必要が出たら、そのときに足す。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EquipmentManagementNoNotDuplicatedCheck.class)
public @interface EquipmentManagementNoNotDuplicated {

	/** 管理番号が重なるときの文（画面設計 12章） */
	String MESSAGE = "この管理番号はすでに使われています";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
