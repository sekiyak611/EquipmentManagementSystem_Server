package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.dao.mapper.custom.EquipmentCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link CategoryNotInUse} の中身。読むだけ（CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class CategoryNotInUseCheck implements ConstraintValidator<CategoryNotInUse, Long> {

	private final EquipmentCustomMapper equipmentCustomMapper;

	@Override
	public boolean isValid(Long categoryId, ConstraintValidatorContext context) {
		if (categoryId == null) {
			return true;
		}
		return equipmentCustomMapper.countByCategoryIdForShare(categoryId) == 0;
	}
}
