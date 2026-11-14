package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.dao.entity.EquipmentExample;
import jp.co.highjunk.equipment.dao.mapper.EquipmentMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.EquipmentCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link EquipmentExists} の中身。読むだけ（CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class EquipmentExistsCheck implements ConstraintValidator<EquipmentExists, Long> {

	private final EquipmentMapper equipmentMapper;

	private final EquipmentCustomMapper equipmentCustomMapper;

	private boolean forUpdate;

	@Override
	public void initialize(EquipmentExists annotation) {
		forUpdate = annotation.forUpdate();
	}

	@Override
	public boolean isValid(Long id, ConstraintValidatorContext context) {
		if (id == null) {
			return true;
		}
		if (forUpdate) {
			return equipmentCustomMapper.selectByIdForUpdate(id) != null;
		}
		EquipmentExample example = new EquipmentExample();
		example.createCriteria().andIdEqualTo(id).andDeletedAtIsNull();
		return equipmentMapper.countByExample(example) > 0;
	}
}
