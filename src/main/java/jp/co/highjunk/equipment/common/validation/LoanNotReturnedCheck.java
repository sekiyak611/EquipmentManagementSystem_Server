package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.dao.entity.Loan;
import jp.co.highjunk.equipment.dao.mapper.custom.LoanCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link LoanNotReturned} の中身。読むだけ（貸出履歴を FOR UPDATE で読む。行を押さえたあとの最新の値を見るため。CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class LoanNotReturnedCheck implements ConstraintValidator<LoanNotReturned, LoanNotReturned.Target> {

	private final LoanCustomMapper loanCustomMapper;

	@Override
	public boolean isValid(LoanNotReturned.Target target, ConstraintValidatorContext context) {
		if (target == null || target.getLoanId() == null) {
			return true;
		}
		Loan loan = loanCustomMapper.selectByIdForUpdate(target.getLoanId());
		if (loan == null || loan.getDeletedAt() != null) {
			return true;
		}
		return loan.getReturnedOn() == null;
	}
}
