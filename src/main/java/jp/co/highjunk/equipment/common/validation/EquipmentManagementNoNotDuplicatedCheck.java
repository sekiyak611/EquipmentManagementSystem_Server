package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.dao.entity.EquipmentExample;
import jp.co.highjunk.equipment.dao.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link EquipmentManagementNoNotDuplicated} の中身。読むだけ（CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class EquipmentManagementNoNotDuplicatedCheck
		implements ConstraintValidator<EquipmentManagementNoNotDuplicated, String> {

	private final EquipmentMapper equipmentMapper;

	@Override
	public boolean isValid(String managementNo, ConstraintValidatorContext context) {
		if (managementNo == null) {
			return true;
		}
		EquipmentExample example = new EquipmentExample();
		example.createCriteria().andManagementNoEqualTo(managementNo).andDeletedAtIsNull();
		return equipmentMapper.countByExample(example) == 0;
	}
}
