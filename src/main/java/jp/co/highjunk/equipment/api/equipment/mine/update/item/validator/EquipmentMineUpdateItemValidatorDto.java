package jp.co.highjunk.equipment.api.equipment.mine.update.item.validator;

import java.util.List;

import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.Loan;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * B-04 の業務チェック（EquipmentMineUpdateItemValidator）に渡す値。ServiceImpl が、読んだ行を入れる（CLAUDE.md 4.1）。
 */
@Getter
@AllArgsConstructor
public class EquipmentMineUpdateItemValidatorDto {

	/** ログインしている人のユーザID */
	private final long loginUserId;

	/** 確認する備品のID（重なりを除いたもの） */
	private final List<Long> equipmentIds;

	/** ServiceImpl が、押さえずに（普通の SELECT で）読んだ、今借りている貸出履歴（送られた備品の分。借りられていない備品の分は、ない） */
	private final List<Loan> currentLoans;

	/** ServiceImpl が FOR UPDATE で押さえて読んだ、削除されていない備品（送られた ID のうち、見つかったもの） */
	private final List<Equipment> equipments;
}
