package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link NewPasswordFormat} の中身。前後の空白も取らずに、そのまま確かめる（スペースも使えるため）。
 */
public class NewPasswordFormatCheck implements ConstraintValidator<NewPasswordFormat, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		String message = messageFor(value);
		if (message == null) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
		return false;
	}

	/** 守れていなければ、画面設計 8章のメッセージ（最初に当てはまった1つ）。守れていれば null。 */
	static String messageFor(String value) {
		if (value == null || value.isEmpty()) {
			return NewPasswordFormat.EMPTY;
		}
		// 半角の英字・数字・記号・スペース（0x20〜0x7E）だけ
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c < 0x20 || c > 0x7E) {
				return NewPasswordFormat.BAD_CHARACTER;
			}
		}
		if (value.length() < NewPasswordFormat.MIN_LENGTH) {
			return NewPasswordFormat.TOO_SHORT;
		}
		if (value.length() > NewPasswordFormat.MAX_LENGTH) {
			return NewPasswordFormat.TOO_LONG;
		}
		return null;
	}
}
