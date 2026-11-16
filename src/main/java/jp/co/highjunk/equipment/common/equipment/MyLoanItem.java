package jp.co.highjunk.equipment.common.equipment;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 自分が借りている備品の1件の形（B-01 の items の1件。詳細設計 equipment/mine/B-01_自分の備品の一覧.md の3章）。
 * MyLoanListAssembler が作り、B-01（B-02・B-04 も同じ形で返す）が自分の RestOutDto に写して返す。
 */
@Getter
@AllArgsConstructor
public class MyLoanItem {

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

	/** 返却予定日を過ぎているか（返却予定日がなければ false） */
	private final boolean overdue;

	/** 最終確認日（未記入なら null） */
	private final LocalDate lastConfirmedOn;

	/** 今月確認済みか */
	private final boolean confirmedThisMonth;
}
