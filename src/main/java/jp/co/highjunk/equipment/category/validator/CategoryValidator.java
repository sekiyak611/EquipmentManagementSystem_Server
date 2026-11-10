package jp.co.highjunk.equipment.category.validator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.error.ErrorItem;
import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.mapper.category.CategoryMapper;

/**
 * 種別の業務チェック（詳細設計 category/K-02_種別を追加する.md の5・6章）。K-02・K-03 で使う。
 * 読むだけで、書き換えない（CLAUDE.md 5章）。
 */
@Component
public class CategoryValidator {

	/** 名前が重なるときの文（画面設計 16章） */
	public static final String NAME_DUPLICATED = "この名前はすでに使われています";

	/** 頭文字が重なるときの文（画面設計 16章） */
	public static final String INITIAL_DUPLICATED = "この頭文字はすでに使われています";

	private final CategoryMapper categoryMapper;

	public CategoryValidator(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	/**
	 * 名前・頭文字が、削除されていないほかの種別と重ならないかを確かめる。
	 *
	 * @param name    名前（前後の空白を取り除いたもの）
	 * @param initial 頭文字（大文字に直したもの）。null なら確かめない
	 * @throws FieldErrorException 重なるとき（400。重なった入力欄ごとの文。名前・頭文字の順）
	 */
	public void validateNotDuplicated(String name, String initial) {
		List<ErrorItem> errors = new ArrayList<>();
		if (categoryMapper.countByName(name) > 0) {
			errors.add(ErrorItem.ofField("name", NAME_DUPLICATED));
		}
		if (initial != null && categoryMapper.countByInitial(initial) > 0) {
			errors.add(ErrorItem.ofField("initial", INITIAL_DUPLICATED));
		}
		if (!errors.isEmpty()) {
			throw new FieldErrorException(errors);
		}
	}
}
