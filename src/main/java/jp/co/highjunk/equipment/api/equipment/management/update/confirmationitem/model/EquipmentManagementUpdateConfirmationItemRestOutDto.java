package jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem.model;

import java.time.LocalDate;

import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatus;
import jp.co.highjunk.equipment.common.equipment.EquipmentSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * C-06 で返す、変えたあとの備品（C-01 の1件と同じ形。詳細設計 equipment/management/C-06_確認した.md の3章）。
 */
@Getter
@AllArgsConstructor
public class EquipmentManagementUpdateConfirmationItemRestOutDto {

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

	/** 最終確認日（今日） */
	private final LocalDate lastConfirmedOn;

	/** 今月確認済みか */
	private final boolean confirmedThisMonth;

	public static EquipmentManagementUpdateConfirmationItemRestOutDto from(EquipmentSummary summary) {
		return new EquipmentManagementUpdateConfirmationItemRestOutDto(summary.getId(), summary.getManagementNo(),
				summary.getCategoryName(), summary.getMaker(), summary.getModel(), summary.getStatus(),
				summary.getLoanStatus(), summary.getBorrowerName(), summary.getLastConfirmedOn(),
				summary.isConfirmedThisMonth());
	}
}
