package jp.co.highjunk.equipment.common.calendar;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

/**
 * 「今月確認済み」の判定（機能一覧 1章の言葉の決まり、詳細設計 common.md 3.11）。
 * 今月確認済み＝最終確認日の月が今月であること。最終確認日が未記入、または先月以前なら「未確認」。
 * 2つ以上の機能（mine・equipment・confirmation・reminder）で使うので、ここに1つだけ置く（CLAUDE.md 5章）。
 * 今日の日付は時計（Clock。common.config）から取る。
 */
@Component
public class MonthlyConfirmationRule {

	private final Clock clock;

	public MonthlyConfirmationRule(Clock clock) {
		this.clock = clock;
	}

	/**
	 * 今月の1日。SQL で「最終確認日がこの日以降なら今月確認済み」と比べるときに使う。
	 * 最終確認日は今日より後にならないので、「1日以降」と「今月」は同じ意味になる。
	 */
	public LocalDate firstDayOfThisMonth() {
		return LocalDate.now(clock).withDayOfMonth(1);
	}

	/**
	 * 今月確認済みか。
	 *
	 * @param lastConfirmedOn 最終確認日（未記入なら null）
	 * @return 最終確認日の月が今月なら true。null や先月以前なら false
	 */
	public boolean isConfirmedThisMonth(LocalDate lastConfirmedOn) {
		if (lastConfirmedOn == null) {
			return false;
		}
		LocalDate today = LocalDate.now(clock);
		return lastConfirmedOn.getYear() == today.getYear() && lastConfirmedOn.getMonth() == today.getMonth();
	}
}
