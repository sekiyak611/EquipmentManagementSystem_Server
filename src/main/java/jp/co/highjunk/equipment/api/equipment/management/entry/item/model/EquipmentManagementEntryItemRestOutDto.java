package jp.co.highjunk.equipment.api.equipment.management.entry.item.model;

import java.time.LocalDate;

import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * C-03 で返す、作った備品（C-01 の1件と同じ形。詳細設計 equipment/management/C-03_備品を登録する.md の3章）。
 */
@Getter
@AllArgsConstructor
public class EquipmentManagementEntryItemRestOutDto {

	/** 備品のID */
	private final long id;

	/** 管理番号 */
	private final String managementNo;

	/** 種別の名前 */
	private final String categoryName;

	/** メーカー（なければ null） */
	private final String maker;

	/** モデル（なければ null） */
	private final String model;

	/** 管理状態（作ったばかりなので管理中） */
	private final EquipmentStatus status;

	/** 貸出状態（作ったばかりなので保管中） */
	private final LoanStatus loanStatus;

	/** 借用者の氏名（作ったばかりなので null） */
	private final String borrowerName;

	/** 最終確認日（登録した日） */
	private final LocalDate lastConfirmedOn;

	/** 今月確認済みか（作ったばかりなので true） */
	private final boolean confirmedThisMonth;
}
