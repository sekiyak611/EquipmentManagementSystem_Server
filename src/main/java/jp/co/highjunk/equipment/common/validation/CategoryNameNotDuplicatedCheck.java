package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.dao.entity.CategoryExample;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link CategoryNameNotDuplicated} の中身。読むだけ（CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class CategoryNameNotDuplicatedCheck
		implements ConstraintValidator<CategoryNameNotDuplicated, CategoryNameNotDuplicated.Target> {

	private final CategoryMapper categoryMapper;

	@Override
	public boolean isValid(CategoryNameNotDuplicated.Target target, ConstraintValidatorContext context) {
		if (target == null || target.getName() == null) {
			return true;
		}
		CategoryExample example = new CategoryExample();
		CategoryExample.Criteria criteria = example.createCriteria().andNameEqualTo(target.getName())
				.andDeletedAtIsNull();
		if (target.getId() != null) {
			criteria.andIdNotEqualTo(target.getId());
		}
		if (categoryMapper.countByExample(example) == 0) {
			return true;
		}
		CategoryChecks.addFieldViolation(context, "name");
		return false;
	}
}
