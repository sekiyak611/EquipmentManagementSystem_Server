package jp.co.highjunk.equipment.api.equipment.management.search.item.model;

import java.time.LocalDate;
import java.util.List;

import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatus;
import jp.co.highjunk.equipment.common.equipment.EquipmentSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * C-01 の返す値（API設計 2.5 のページ分けの形。詳細設計 equipment/management/C-01_備品を探す.md の3章）。
 */
@Getter
@AllArgsConstructor
public class EquipmentManagementSearchItemRestOutDto {

	/** このページの備品 */
	private final List<Item> items;

	/** 何ページ目か */
	private final int page;

	/** 1ページの件数（50） */
	private final int size;

	/** 条件に合う備品の数 */
	private final long totalCount;

	/** ページの数（0件なら 0） */
	private final int totalPages;

	/** items の1件 */
	@Getter
	@AllArgsConstructor
	public static class Item {

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

		public static Item from(EquipmentSummary summary) {
			return new Item(summary.getId(), summary.getManagementNo(), summary.getCategoryName(), summary.getMaker(),
					summary.getModel(), summary.getStatus(), summary.getLoanStatus(), summary.getBorrowerName(),
					summary.getLastConfirmedOn(), summary.isConfirmedThisMonth());
		}
	}
}
