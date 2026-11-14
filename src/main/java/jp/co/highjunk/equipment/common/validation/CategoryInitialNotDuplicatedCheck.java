package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.dao.entity.CategoryExample;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link CategoryInitialNotDuplicated} の中身。読むだけ（CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class CategoryInitialNotDuplicatedCheck
		implements ConstraintValidator<CategoryInitialNotDuplicated, CategoryInitialNotDuplicated.Target> {

	private final CategoryMapper categoryMapper;

	@Override
	public boolean isValid(CategoryInitialNotDuplicated.Target target, ConstraintValidatorContext context) {
		if (target == null || target.getInitial() == null) {
			return true;
		}
		CategoryExample example = new CategoryExample();
		CategoryExample.Criteria criteria = example.createCriteria().andInitialEqualTo(target.getInitial())
				.andDeletedAtIsNull();
		if (target.getId() != null) {
			criteria.andIdNotEqualTo(target.getId());
		}
		if (categoryMapper.countByExample(example) == 0) {
			return true;
		}
		CategoryChecks.addFieldViolation(context, "initial");
		return false;
	}
}
