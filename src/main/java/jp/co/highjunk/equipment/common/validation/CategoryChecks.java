package jp.co.highjunk.equipment.common.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;

import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.common.error.ErrorItem;
import jp.co.highjunk.equipment.common.error.FieldErrorException;

/**
 * 種別のチェックで共通に使う道具（K-02・K-03 で使う。CLAUDE.md 4.1）。
 */
public final class CategoryChecks {

	/** 名前の重複禁止の名前（V1__create_tables.sql） */
	private static final String NAME_UNIQUE_KEY = "uk_categories_name_active";

	/** 頭文字の重複禁止の名前（V1__create_tables.sql） */
	private static final String INITIAL_UNIQUE_KEY = "uk_categories_initial_active";

	private CategoryChecks() {
	}

	/**
	 * 種別の重複禁止に当たった例外を、入力欄のエラーに変える（詳細設計 equipment/category/K-02_種別を追加する.md の8章）。
	 * チェック（{@link CategoryNameNotDuplicated}・{@link CategoryInitialNotDuplicated}）のあとで、
	 * ほかの人が同じ名前・頭文字にしていたときに起きる。
	 * どの重複禁止に当たったかを、例外の文の中の制約の名前で見分ける。見分けられなければ、受け取った例外をそのまま返す（投げ直すと 500）。
	 */
	public static RuntimeException duplicateKeyToFieldError(DuplicateKeyException e) {
		String detail = String.valueOf(e.getMessage());
		List<ErrorItem> errors = new ArrayList<>();
		if (detail.contains(NAME_UNIQUE_KEY)) {
			errors.add(ErrorItem.ofField("name", CategoryNameNotDuplicated.MESSAGE));
		}
		if (detail.contains(INITIAL_UNIQUE_KEY)) {
			errors.add(ErrorItem.ofField("initial", CategoryInitialNotDuplicated.MESSAGE));
		}
		return errors.isEmpty() ? e : new FieldErrorException(errors);
	}

	/** クラスに付けたチェックの誤りを、入力欄（field）のエラーとして出す。 */
	static void addFieldViolation(ConstraintValidatorContext context, String field) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
				.addPropertyNode(field).addConstraintViolation();
	}
}
