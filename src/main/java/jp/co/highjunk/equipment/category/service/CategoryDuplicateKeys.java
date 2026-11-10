package jp.co.highjunk.equipment.category.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;

import jp.co.highjunk.equipment.category.validator.CategoryValidator;
import jp.co.highjunk.equipment.common.error.ErrorItem;
import jp.co.highjunk.equipment.common.error.FieldErrorException;

/**
 * 種別の重複禁止に当たった例外を、入力欄のエラーに変える（詳細設計 category/K-02_種別を追加する.md の8章・12章の No.6）。
 * K-02・K-03 の ServiceImpl が使う。確かめ（CategoryValidator）のあとで、ほかの人が同じ名前・頭文字にしていたときに起きる。
 */
final class CategoryDuplicateKeys {

	/** 名前の重複禁止の名前（V1__create_tables.sql） */
	private static final String NAME_UNIQUE_KEY = "uk_categories_name_active";

	/** 頭文字の重複禁止の名前（V1__create_tables.sql） */
	private static final String INITIAL_UNIQUE_KEY = "uk_categories_initial_active";

	private CategoryDuplicateKeys() {
	}

	/**
	 * どの重複禁止に当たったかを、例外の文の中の制約の名前で見分けて、入力欄のエラーにする。
	 * 見分けられなければ、受け取った例外をそのまま返す（投げ直すと 500）。
	 */
	static RuntimeException toFieldError(DuplicateKeyException e) {
		String detail = String.valueOf(e.getMessage());
		List<ErrorItem> errors = new ArrayList<>();
		if (detail.contains(NAME_UNIQUE_KEY)) {
			errors.add(ErrorItem.ofField("name", CategoryValidator.NAME_DUPLICATED));
		}
		if (detail.contains(INITIAL_UNIQUE_KEY)) {
			errors.add(ErrorItem.ofField("initial", CategoryValidator.INITIAL_DUPLICATED));
		}
		return errors.isEmpty() ? e : new FieldErrorException(errors);
	}
}
