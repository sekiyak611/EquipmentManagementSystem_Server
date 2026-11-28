package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.dao.entity.RoleExample;
import jp.co.highjunk.equipment.dao.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link RoleExists} の中身。読むだけ（CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class RoleExistsCheck implements ConstraintValidator<RoleExists, Long> {

	private final RoleMapper roleMapper;

	@Override
	public boolean isValid(Long id, ConstraintValidatorContext context) {
		if (id == null) {
			return true;
		}
		RoleExample example = new RoleExample();
		example.createCriteria().andIdEqualTo(id).andDeletedAtIsNull();
		return roleMapper.countByExample(example) > 0;
	}
}
