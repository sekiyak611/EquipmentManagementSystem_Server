package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link EquipmentManaged} の中身。読むだけ（CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class EquipmentManagedCheck implements ConstraintValidator<EquipmentManaged, Long> {

	private final EquipmentMapper equipmentMapper;

	@Override
	public boolean isValid(Long id, ConstraintValidatorContext context) {
		if (id == null) {
			return true;
		}
		Equipment equipment = equipmentMapper.selectByPrimaryKey(id);
		if (equipment == null || equipment.getDeletedAt() != null) {
			return true;
		}
		return EquipmentStatus.MANAGED.name().equals(equipment.getStatus());
	}
}
