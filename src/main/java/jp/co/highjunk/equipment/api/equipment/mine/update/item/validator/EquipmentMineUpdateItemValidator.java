package jp.co.highjunk.equipment.api.equipment.mine.update.item.validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.common.error.ForbiddenException;
import jp.co.highjunk.equipment.common.error.NotFoundException;
import jp.co.highjunk.equipment.common.validation.EquipmentManaged;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.Loan;

/**
 * B-04 の業務チェック：送られた備品が、すべて、ある・自分が借りている・管理中であること
 * （詳細設計 equipment/mine/B-04_確認した.md の5・6章）。
 * ServiceImpl が読んだ備品（FOR UPDATE で押さえた行）・貸出履歴（押さえずに読んだ行）を見て確かめるので、ServiceInDto のチェックではなく Validator で行う
 * （CLAUDE.md 5章）。読むだけ（Mapper を使わない。書き換えない）。
 */
@Component
public class EquipmentMineUpdateItemValidator {

	/** 自分が借りていない備品が含まれていたときの文（画面設計 3章。2026-11-23 に人が決めた） */
	public static final String NOT_BORROWED_BY_SELF = "借りていない備品が含まれているため、確認できません。";

	/**
	 * 守れないものが2つ以上あるときは、404 → 403 → 400 の順に、先のもの1つを投げる（common.md 3.20）。
	 *
	 * @throws NotFoundException     備品がない・削除済み（404）
	 * @throws ForbiddenException    今借りている貸出履歴がない、または借りたのが自分でない（403）
	 * @throws BusinessRuleException 管理中でない（400）
	 */
	public void check(EquipmentMineUpdateItemValidatorDto validatorDto) {
		Set<Long> foundEquipmentIds = new HashSet<>();
		for (Equipment equipment : validatorDto.getEquipments()) {
			foundEquipmentIds.add(equipment.getId());
		}
		if (!foundEquipmentIds.containsAll(validatorDto.getEquipmentIds())) {
			throw new NotFoundException();
		}

		Map<Long, Loan> currentLoanByEquipmentId = new HashMap<>();
		for (Loan loan : validatorDto.getCurrentLoans()) {
			currentLoanByEquipmentId.put(loan.getEquipmentId(), loan);
		}
		for (Long equipmentId : validatorDto.getEquipmentIds()) {
			Loan loan = currentLoanByEquipmentId.get(equipmentId);
			if (loan == null || loan.getUserId() == null || loan.getUserId() != validatorDto.getLoginUserId()) {
				throw new ForbiddenException(NOT_BORROWED_BY_SELF);
			}
		}

		for (Equipment equipment : validatorDto.getEquipments()) {
			if (!EquipmentStatus.MANAGED.name().equals(equipment.getStatus())) {
				throw new BusinessRuleException(EquipmentManaged.MESSAGE);
			}
		}
	}
}
