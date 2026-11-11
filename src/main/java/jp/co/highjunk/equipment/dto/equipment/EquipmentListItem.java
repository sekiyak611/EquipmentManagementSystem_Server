package jp.co.highjunk.equipment.dto.equipment;

import java.time.LocalDate;

import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatus;

/**
 * C-01 の items の1件（詳細設計 equipment/C-01_備品を探す.md の3章）。
 *
 * @param id                 備品のID
 * @param managementNo       管理番号
 * @param categoryName       種別の名前
 * @param maker              メーカー（なければ null）
 * @param model              モデル（なければ null）
 * @param status             管理状態
 * @param loanStatus         貸出状態
 * @param borrowerName       借用者の氏名（貸出中でなければ null）
 * @param lastConfirmedOn    最終確認日（未記入なら null）
 * @param confirmedThisMonth 今月確認済みか
 */
public record EquipmentListItem(long id, String managementNo, String categoryName, String maker, String model,
		EquipmentStatus status, LoanStatus loanStatus, String borrowerName, LocalDate lastConfirmedOn,
		boolean confirmedThisMonth) {
}
