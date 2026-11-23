package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.dao.entity.LoanExample;
import jp.co.highjunk.equipment.dao.mapper.LoanMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.LoanCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link LoanExists} の中身。読むだけ（CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class LoanExistsCheck implements ConstraintValidator<LoanExists, Long> {

	private final LoanMapper loanMapper;

	private final LoanCustomMapper loanCustomMapper;

	private boolean forUpdate;

	@Override
	public void initialize(LoanExists annotation) {
		forUpdate = annotation.forUpdate();
	}

	@Override
	public boolean isValid(Long id, ConstraintValidatorContext context) {
		if (id == null) {
			return true;
		}
		if (forUpdate) {
			return loanCustomMapper.selectByIdForUpdate(id) != null;
		}
		LoanExample example = new LoanExample();
		example.createCriteria().andIdEqualTo(id).andDeletedAtIsNull();
		return loanMapper.countByExample(example) > 0;
	}
}
