package jp.co.highjunk.equipment.common.equipment;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 自分が借りている備品の一覧と、今月の確認の案内（B-01 の data。B-04 も同じ形で返す）。
 */
@Getter
@AllArgsConstructor
public class MyLoanList {

	/** 今月まだ確認していない備品の数（items のうち confirmedThisMonth が false のもの） */
	private final int unconfirmedCount;

	/** 今月の催促の日時（日本時間） */
	private final OffsetDateTime reminderAt;

	/** 今月の催促の日時を過ぎたか */
	private final boolean reminderPassed;

	/** 借りている備品。未確認を上にし、その中は貸出日の古い順 */
	private final List<MyLoanItem> items;
}
