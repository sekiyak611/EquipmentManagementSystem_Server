package jp.co.highjunk.equipment.common.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ReminderCalendar の単体テスト（詳細設計 common.md 3.18）。
 * 時計を止め、祝日は決めた日だけの偽物（HolidayCalendar を継いだもの）で確かめる。データベースは使わない。
 */
@DisplayName("ReminderCalendar（単体）")
class ReminderCalendarTest {

	private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

	private static final String DAILY_9 = "0 0 9 * * *";

	/** 祝日を決めた日だけにした HolidayCalendar */
	private static HolidayCalendar holidays(LocalDate... days) {
		Set<LocalDate> set = Set.of(days);
		return new HolidayCalendar(null) {
			@Override
			public boolean isHoliday(LocalDate date) {
				return set.contains(date);
			}
		};
	}

	private static Clock clockAt(int year, int month, int day) {
		return Clock.fixed(ZonedDateTime.of(year, month, day, 10, 0, 0, 0, TOKYO).toInstant(), TOKYO);
	}

	private static ReminderCalendar calendar(String cron, LocalDate... holidays) {
		return new ReminderCalendar(holidays(holidays), clockAt(2026, 11, 16), cron);
	}

	@Test
	@DisplayName("25日が平日なら、その日が催促日")
	void businessDay() {
		ReminderCalendar calendar = calendar(DAILY_9);
		// 2026-11-25 は水曜
		assertThat(calendar.reminderDate(YearMonth.of(2026, 11))).isEqualTo(LocalDate.of(2026, 11, 25));
		// 月曜（2026-05-25）・金曜（2026-12-25）
		assertThat(calendar.reminderDate(YearMonth.of(2026, 5))).isEqualTo(LocalDate.of(2026, 5, 25));
		assertThat(calendar.reminderDate(YearMonth.of(2026, 12))).isEqualTo(LocalDate.of(2026, 12, 25));
	}

	@Test
	@DisplayName("25日が土曜なら金曜、日曜なら金曜（前の平日）")
	void weekend() {
		ReminderCalendar calendar = calendar(DAILY_9);
		// 2026-07-25 は土曜、2026-10-25 は日曜
		assertThat(calendar.reminderDate(YearMonth.of(2026, 7))).isEqualTo(LocalDate.of(2026, 7, 24));
		assertThat(calendar.reminderDate(YearMonth.of(2026, 10))).isEqualTo(LocalDate.of(2026, 10, 23));
	}

	@Test
	@DisplayName("25日が祝日なら、前の平日。その日も祝日なら、さらに前の平日")
	void holiday() {
		// 2026-11-25（水）が祝日 → 24日（火）
		assertThat(calendar(DAILY_9, LocalDate.of(2026, 11, 25)).reminderDate(YearMonth.of(2026, 11)))
				.isEqualTo(LocalDate.of(2026, 11, 24));
		// 25日・24日・23日（月）が祝日、22日（日）・21日（土）→ 20日（金）
		assertThat(calendar(DAILY_9, LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 24), LocalDate.of(2026, 11, 23))
				.reminderDate(YearMonth.of(2026, 11))).isEqualTo(LocalDate.of(2026, 11, 20));
	}

	@Test
	@DisplayName("催促の日時は、催促日の9:00（日本時間）")
	void reminderAtDefault() {
		ZonedDateTime at = calendar(DAILY_9).reminderAt(YearMonth.of(2026, 7));
		assertThat(at.toLocalDateTime()).isEqualTo(LocalDate.of(2026, 7, 24).atTime(9, 0));
		assertThat(at.getZone()).isEqualTo(TOKYO);
		assertThat(at.toOffsetDateTime().getOffset().getTotalSeconds()).isEqualTo(9 * 3600);
	}

	@Test
	@DisplayName("催促の時刻は、設定の cron の時刻に従う（毎日1回の形のとき）")
	void reminderTimeFromCron() {
		assertThat(calendar("0 30 8 * * *").reminderAt(YearMonth.of(2026, 11)).toLocalTime()).isEqualTo(LocalTime.of(8, 30));
		assertThat(calendar("15 5 23 * * ?").reminderAt(YearMonth.of(2026, 11)).toLocalTime()).isEqualTo(LocalTime.of(23, 5, 15));
		assertThat(calendar("0 0 0 * * *").reminderAt(YearMonth.of(2026, 11)).toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
	}

	@Test
	@DisplayName("毎日1回の形でない cron（曜日の指定・毎時など）のときは、9:00")
	void reminderTimeFallback() {
		assertThat(calendar("0 0 9 * * MON-FRI").reminderAt(YearMonth.of(2026, 11)).toLocalTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(calendar("0 0 * * * *").reminderAt(YearMonth.of(2026, 11)).toLocalTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(calendar("0 0 8,12 * * *").reminderAt(YearMonth.of(2026, 11)).toLocalTime()).isEqualTo(LocalTime.of(9, 0));
	}

	@Test
	@DisplayName("cron の書き方が誤っていれば、作れない（起動のときに止まる）")
	void invalidCron() {
		assertThatThrownBy(() -> calendar("毎朝9時")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> calendar("0 0 9 *")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("今月の催促日・催促の日時は、時計の今日から決まる")
	void thisMonth() {
		ReminderCalendar calendar = new ReminderCalendar(holidays(), clockAt(2026, 7, 1), DAILY_9);
		assertThat(calendar.reminderDateOfThisMonth()).isEqualTo(LocalDate.of(2026, 7, 24));
		assertThat(calendar.reminderAtOfThisMonth().toLocalDateTime()).isEqualTo(LocalDate.of(2026, 7, 24).atTime(9, 0));
		// 月末・年の変わり目（12月31日 → 12月の催促日）
		ReminderCalendar december = new ReminderCalendar(holidays(), clockAt(2026, 12, 31), DAILY_9);
		assertThat(december.reminderDateOfThisMonth()).isEqualTo(LocalDate.of(2026, 12, 25));
	}

	private static ReminderCalendar calendarAt(int month, int day, int hour, int minute, int second, String cron,
			LocalDate... holidays) {
		ZonedDateTime now = ZonedDateTime.of(2026, month, day, hour, minute, second, 0, TOKYO);
		return new ReminderCalendar(holidays(holidays), Clock.fixed(now.toInstant(), TOKYO), cron);
	}

	@Test
	@DisplayName("今月の催促の日時を過ぎたか：催促日の前の日・当日の催促の時刻の前は false、ちょうどの時刻と、あとは true")
	void isReminderPassed() {
		// 2026-11 の催促は 11/25（水）9:00
		assertThat(calendarAt(11, 1, 0, 0, 0, DAILY_9).isReminderPassed()).isFalse();
		assertThat(calendarAt(11, 24, 23, 59, 59, DAILY_9).isReminderPassed()).isFalse();
		assertThat(calendarAt(11, 25, 8, 59, 59, DAILY_9).isReminderPassed()).isFalse();
		assertThat(calendarAt(11, 25, 9, 0, 0, DAILY_9).isReminderPassed()).isTrue();
		assertThat(calendarAt(11, 25, 18, 0, 0, DAILY_9).isReminderPassed()).isTrue();
		assertThat(calendarAt(11, 30, 23, 59, 59, DAILY_9).isReminderPassed()).isTrue();
	}

	@Test
	@DisplayName("今月の催促の日時を過ぎたか：25日が土日なら前の平日の9:00。設定の時刻にも従う")
	void isReminderPassedShifted() {
		// 2026-07 の催促は 7/24（金）9:00（25日は土曜）
		assertThat(calendarAt(7, 24, 8, 59, 59, DAILY_9).isReminderPassed()).isFalse();
		assertThat(calendarAt(7, 24, 9, 0, 0, DAILY_9).isReminderPassed()).isTrue();
		// 7/25（土）・7/26（日）は、催促の日時のあと
		assertThat(calendarAt(7, 25, 12, 0, 0, DAILY_9).isReminderPassed()).isTrue();
		// 設定の時刻 8:30 なら、8:30 から
		assertThat(calendarAt(7, 24, 8, 29, 59, "0 30 8 * * *").isReminderPassed()).isFalse();
		assertThat(calendarAt(7, 24, 8, 30, 0, "0 30 8 * * *").isReminderPassed()).isTrue();
	}

	@Test
	@DisplayName("催促を送ってよい日：催促日の前は false、当日と、あとの平日は true")
	void isReminderDue() {
		// 2026-11 の催促日は 25日（水）
		ReminderCalendar calendar = calendar(DAILY_9);
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 11, 1))).isFalse();
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 11, 24))).isFalse();
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 11, 25))).isTrue();
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 11, 26))).isTrue();
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 11, 30))).isTrue();
	}

	@Test
	@DisplayName("催促を送ってよい日：土日祝日は false（催促日があとの日でも）")
	void isReminderDueNotBusinessDay() {
		// 2026-07 の催促日は 24日（金）。25日（土）・26日（日）は送らない。27日（月）は、24日のあとの平日
		ReminderCalendar calendar = calendar(DAILY_9, LocalDate.of(2026, 7, 28));
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 7, 23))).isFalse();
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 7, 24))).isTrue();
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 7, 25))).isFalse();
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 7, 26))).isFalse();
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 7, 27))).isTrue();
		// 祝日に決めた日
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 7, 28))).isFalse();
	}

	@Test
	@DisplayName("催促を送ってよい日：月が替わると、また催促日の前に戻る")
	void isReminderDueMonthChange() {
		ReminderCalendar calendar = calendar(DAILY_9);
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 11, 30))).isTrue();
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 12, 1))).isFalse();
		// 年またぎ：2026-12 の催促日は 25日（金）
		assertThat(calendar.isReminderDue(LocalDate.of(2026, 12, 25))).isTrue();
		assertThat(calendar.isReminderDue(LocalDate.of(2027, 1, 4))).isFalse();
	}
}
