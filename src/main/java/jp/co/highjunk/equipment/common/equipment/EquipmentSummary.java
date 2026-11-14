package jp.co.highjunk.equipment.common.equipment;

import java.time.LocalDate;

import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 備品の一覧の1件の形（C-01 の items の1件。詳細設計 equipment/management/C-01_備品を探す.md の3章）。
 * EquipmentSummaryAssembler が作り、C-01・C-03・C-06 が自分の RestOutDto に写して返す。
 */
@Getter
@AllArgsConstructor
public class EquipmentSummary {

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

	/** 管理状態 */
	private final EquipmentStatus status;

	/** 貸出状態 */
	private final LoanStatus loanStatus;

	/** 借用者の氏名（貸出中でなければ null） */
	private final String borrowerName;

	/** 最終確認日（未記入なら null） */
	private final LocalDate lastConfirmedOn;

	/** 今月確認済みか */
	private final boolean confirmedThisMonth;
}
