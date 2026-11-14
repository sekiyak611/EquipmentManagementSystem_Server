package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.dao.entity.CategoryExample;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.CategoryCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link CategoryExists} の中身。読むだけ（CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class CategoryExistsCheck implements ConstraintValidator<CategoryExists, Long> {

	private final CategoryMapper categoryMapper;

	private final CategoryCustomMapper categoryCustomMapper;

	private boolean forUpdate;

	@Override
	public void initialize(CategoryExists annotation) {
		forUpdate = annotation.forUpdate();
	}

	@Override
	public boolean isValid(Long id, ConstraintValidatorContext context) {
		if (id == null) {
			return true;
		}
		if (forUpdate) {
			return categoryCustomMapper.selectByIdForUpdate(id) != null;
		}
		CategoryExample example = new CategoryExample();
		example.createCriteria().andIdEqualTo(id).andDeletedAtIsNull();
		return categoryMapper.countByExample(example) > 0;
	}
}
