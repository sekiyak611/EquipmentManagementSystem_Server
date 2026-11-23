package jp.co.highjunk.equipment.api.equipment.mine.update.item.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import jp.co.highjunk.equipment.common.equipment.MyLoanItem;
import jp.co.highjunk.equipment.common.equipment.MyLoanList;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * B-04 で返す、確認したあとの最新の「自分の備品」一覧（詳細設計 equipment/mine/B-04_確認した.md の3章）。
 * B-01 の data と同じ形（API のパッケージどうしは使い合わないので、同じ形を、ここにも書く）。確認した備品も、借りている間は items に出る（確認済みになる）。
 */
@Getter
@AllArgsConstructor
public class EquipmentMineUpdateItemRestOutDto {

	/** 今月まだ確認していない備品の数 */
	private final int unconfirmedCount;

	/** 今月の催促の日時（日本時間。例：2026-11-25T09:00:00+09:00） */
	private final OffsetDateTime reminderAt;

	/** 今月の催促の日時を過ぎたか */
	private final boolean reminderPassed;

	/** 借りている備品。未確認を上にし、その中は貸出日の古い順 */
	private final List<Item> items;

	public static EquipmentMineUpdateItemRestOutDto from(MyLoanList list) {
		return new EquipmentMineUpdateItemRestOutDto(list.getUnconfirmedCount(), list.getReminderAt(),
				list.isReminderPassed(), list.getItems().stream().map(Item::from).toList());
	}

	/** 借りている備品の1件 */
	@Getter
	@AllArgsConstructor
	public static class Item {

		/** 貸出履歴のID */
		private final long loanId;

		/** 備品のID */
		private final long equipmentId;

		/** 管理番号 */
		private final String managementNo;

		/** 種別の名前 */
		private final String categoryName;

		/** メーカー（なければ null） */
		private final String maker;

		/** モデル（なければ null） */
		private final String model;

		/** 貸出日 */
		private final LocalDate lentOn;

		/** 返却予定日（なければ null） */
		private final LocalDate dueOn;

		/** 返却予定日を過ぎているか */
		private final boolean overdue;

		/** 最終確認日（未記入なら null） */
		private final LocalDate lastConfirmedOn;

		/** 今月確認済みか */
		private final boolean confirmedThisMonth;

		static Item from(MyLoanItem item) {
			return new Item(item.getLoanId(), item.getEquipmentId(), item.getManagementNo(), item.getCategoryName(),
					item.getMaker(), item.getModel(), item.getLentOn(), item.getDueOn(), item.isOverdue(),
					item.getLastConfirmedOn(), item.isConfirmedThisMonth());
		}
	}
}
