package jp.co.highjunk.equipment.common.validation;

import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link PasswordConfirmed} の中身。パスワードがない（null）ときは、ここでは確かめない（password 側のエラーが出るため）。
 */
public class PasswordConfirmedCheck implements ConstraintValidator<PasswordConfirmed, PasswordConfirmable> {

	@Override
	public boolean isValid(PasswordConfirmable value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		// ない（null）ものは、空と同じに扱う（どちらも空なら、一致。空のエラーは password 側が出す）
		String password = Objects.requireNonNullElse(value.getPassword(), "");
		String confirm = Objects.requireNonNullElse(value.getPasswordConfirm(), "");
		if (password.equals(confirm)) {
			return true;
		}
		// 誤りは、クラスではなく、入力欄 passwordConfirm に付ける
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
				.addPropertyNode("passwordConfirm").addConstraintViolation();
		return false;
	}
}
