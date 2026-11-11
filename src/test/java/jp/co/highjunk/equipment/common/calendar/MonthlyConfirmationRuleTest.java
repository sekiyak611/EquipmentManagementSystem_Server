package jp.co.highjunk.equipment.common.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

/** MonthlyConfirmationRule の単体テスト（詳細設計 common.md 3.11）。時計を止めて確かめる */
class MonthlyConfirmationRuleTest {

	private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

	/** 日本時間のその日時で止まった時計 */
	private static MonthlyConfirmationRule at(int year, int month, int day, int hour, int minute) {
		ZonedDateTime now = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, TOKYO);
		return new MonthlyConfirmationRule(Clock.fixed(now.toInstant(), TOKYO));
	}

	@Test
	void 今月の1日を返す() {
		assertThat(at(2026, 11, 11, 10, 0).firstDayOfThisMonth()).isEqualTo(LocalDate.of(2026, 11, 1));
		// 月の初日・末日・年の変わり目
		assertThat(at(2026, 11, 1, 0, 0).firstDayOfThisMonth()).isEqualTo(LocalDate.of(2026, 11, 1));
		assertThat(at(2026, 12, 31, 23, 59).firstDayOfThisMonth()).isEqualTo(LocalDate.of(2026, 12, 1));
	}

	@Test
	void 最終確認日が今月なら確認済み() {
		MonthlyConfirmationRule rule = at(2026, 11, 11, 10, 0);
		assertThat(rule.isConfirmedThisMonth(LocalDate.of(2026, 11, 1))).isTrue();
		assertThat(rule.isConfirmedThisMonth(LocalDate.of(2026, 11, 11))).isTrue();
	}

	@Test
	void 最終確認日が未記入や先月以前なら未確認() {
		MonthlyConfirmationRule rule = at(2026, 11, 11, 10, 0);
		assertThat(rule.isConfirmedThisMonth(null)).isFalse();
		assertThat(rule.isConfirmedThisMonth(LocalDate.of(2026, 10, 31))).isFalse();
		// 去年の同じ月は今月ではない
		assertThat(rule.isConfirmedThisMonth(LocalDate.of(2025, 11, 11))).isFalse();
	}

	@Test
	void 日本時間で判定する() {
		// 日本時間の 2026-12-01 0:30 は、UTC ではまだ 11月30日。日本時間の12月として判定する
		MonthlyConfirmationRule rule = at(2026, 12, 1, 0, 30);
		assertThat(rule.firstDayOfThisMonth()).isEqualTo(LocalDate.of(2026, 12, 1));
		assertThat(rule.isConfirmedThisMonth(LocalDate.of(2026, 11, 30))).isFalse();
		assertThat(rule.isConfirmedThisMonth(LocalDate.of(2026, 12, 1))).isTrue();
	}
}
