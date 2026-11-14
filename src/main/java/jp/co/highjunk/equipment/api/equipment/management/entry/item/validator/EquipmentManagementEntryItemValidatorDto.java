package jp.co.highjunk.equipment.api.equipment.management.entry.item.validator;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * C-03 の業務チェック（EquipmentManagementEntryItemValidator）に渡す値。ServiceImpl が、受け取った日付と今日の日付を入れる（CLAUDE.md 4.1）。
 */
@Getter
@AllArgsConstructor
public class EquipmentManagementEntryItemValidatorDto {

	/** 購入日（なければ null） */
	private final LocalDate purchasedOn;

	/** 保証期日（なければ null） */
	private final LocalDate warrantyUntil;

	/** 今日（ServiceImpl が Clock から取った日付） */
	private final LocalDate today;
}
