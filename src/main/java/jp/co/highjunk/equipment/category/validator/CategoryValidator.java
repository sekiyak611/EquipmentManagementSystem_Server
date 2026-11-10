package jp.co.highjunk.equipment.category.validator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.category.model.Category;
import jp.co.highjunk.equipment.common.error.ConflictException;
import jp.co.highjunk.equipment.common.error.ErrorItem;
import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.common.error.NotFoundException;
import jp.co.highjunk.equipment.mapper.category.CategoryMapper;

/**
 * 種別の業務チェック（詳細設計 category/K-02_種別を追加する.md・K-03_種別を変える.md の5・6章）。K-02・K-03 で使う。
 * 読むだけで、書き換えない（CLAUDE.md 5章）。
 */
@Component
public class CategoryValidator {

	/** 名前が重なるときの文（画面設計 16章） */
	public static final String NAME_DUPLICATED = "この名前はすでに使われています";

	/** 頭文字が重なるときの文（画面設計 16章） */
	public static final String INITIAL_DUPLICATED = "この頭文字はすでに使われています";

	/** ほかの人が先に保存していたときの文（画面設計 16章） */
	public static final String UPDATED_BY_OTHERS = "ほかの人が先にこの種別を更新しました。画面を開き直して、最新の内容を確かめてください。";

	private final CategoryMapper categoryMapper;

	public CategoryValidator(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	/**
	 * 変えてよい種別かを確かめる（K-03）。削除されていない種別があり、version が画面を開いたときと同じこと。
	 *
	 * @return 読んだ種別
	 * @throws NotFoundException ない・削除済みのとき（404）
	 * @throws ConflictException version が違う（ほかの人が先に保存した）とき（409）
	 */
	public Category validateUpdatable(long id, int version) {
		Category category = categoryMapper.selectById(id);
		if (category == null) {
			throw new NotFoundException();
		}
		if (category.getVersion() != version) {
			throw new ConflictException(UPDATED_BY_OTHERS);
		}
		return category;
	}

	/**
	 * 名前・頭文字が、削除されていないほかの種別と重ならないかを確かめる。
	 *
	 * @param name      名前（前後の空白を取り除いたもの）
	 * @param initial   頭文字（大文字に直したもの）。null なら確かめない
	 * @param excludeId 数えない種別の ID（K-03 で、変える種別自身）。K-02 は null
	 * @throws FieldErrorException 重なるとき（400。重なった入力欄ごとの文。名前・頭文字の順）
	 */
	public void validateNotDuplicated(String name, String initial, Long excludeId) {
		List<ErrorItem> errors = new ArrayList<>();
		if (categoryMapper.countByName(name, excludeId) > 0) {
			errors.add(ErrorItem.ofField("name", NAME_DUPLICATED));
		}
		if (initial != null && categoryMapper.countByInitial(initial, excludeId) > 0) {
			errors.add(ErrorItem.ofField("initial", INITIAL_DUPLICATED));
		}
		if (!errors.isEmpty()) {
			throw new FieldErrorException(errors);
		}
	}
}
