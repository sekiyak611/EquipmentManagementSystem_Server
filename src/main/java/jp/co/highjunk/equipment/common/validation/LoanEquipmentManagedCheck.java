package jp.co.highjunk.equipment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.Loan;
import jp.co.highjunk.equipment.dao.mapper.custom.EquipmentCustomMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.LoanCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * {@link LoanEquipmentManaged} の中身。読むだけ（貸出履歴・備品を FOR UPDATE で読む。行を押さえたあとの最新の値を見るため。CLAUDE.md 5章）。
 */
@RequiredArgsConstructor
public class LoanEquipmentManagedCheck implements ConstraintValidator<LoanEquipmentManaged, LoanEquipmentManaged.Target> {

	private final LoanCustomMapper loanCustomMapper;

	private final EquipmentCustomMapper equipmentCustomMapper;

	@Override
	public boolean isValid(LoanEquipmentManaged.Target target, ConstraintValidatorContext context) {
		if (target == null || target.getLoanId() == null) {
			return true;
		}
		Loan loan = loanCustomMapper.selectByIdForUpdate(target.getLoanId());
		if (loan == null || loan.getDeletedAt() != null) {
			return true;
		}
		Equipment equipment = equipmentCustomMapper.selectByIdForUpdate(loan.getEquipmentId());
		return equipment != null && EquipmentStatus.MANAGED.name().equals(equipment.getStatus());
	}
}
