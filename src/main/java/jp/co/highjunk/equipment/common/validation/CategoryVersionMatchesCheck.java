package jp.co.highjunk.equipment.common.validation;

import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.dao.entity.Category;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link CategoryVersionMatches} の中身。読むだけ（CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class CategoryVersionMatchesCheck
		implements ConstraintValidator<CategoryVersionMatches, CategoryVersionMatches.Target> {

	private final CategoryMapper categoryMapper;

	@Override
	public boolean isValid(CategoryVersionMatches.Target target, ConstraintValidatorContext context) {
		if (target == null || target.getId() == null) {
			return true;
		}
		Category category = categoryMapper.selectByPrimaryKey(target.getId());
		if (category == null || category.getDeletedAt() != null) {
			return true;
		}
		return Objects.equals(category.getVersion(), target.getVersion());
	}
}
