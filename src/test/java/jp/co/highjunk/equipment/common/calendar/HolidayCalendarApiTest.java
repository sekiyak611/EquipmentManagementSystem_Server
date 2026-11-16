package jp.co.highjunk.equipment.common.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.dao.entity.Holiday;
import jp.co.highjunk.equipment.dao.mapper.HolidayMapper;

/**
 * HolidayCalendar・ReminderCalendar のテスト（詳細設計 common.md 3.18）。
 * Flyway の V3 で入れた祝日（内閣府の CSV）と、データベースを使う。書き込むテストは、終わるたびに元に戻す（@Transactional）。
 */
@SpringBootTest
@Transactional
@DisplayName("HolidayCalendar・ReminderCalendar（データベースを使う）")
class HolidayCalendarApiTest {

	private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

	@Autowired
	HolidayCalendar holidayCalendar;

	@Autowired
	ReminderCalendar reminderCalendar;

	@Autowired
	HolidayMapper holidayMapper;

	@Autowired
	JdbcTemplate jdbc;

	private void addHoliday(LocalDate day) {
		Holiday row = new Holiday();
		row.setHolidayOn(day);
		row.setName("テスト用の祝日");
		holidayMapper.insert(row);
	}

	@Test
	@DisplayName("V3 で、内閣府の CSV の全件（1,067件。1955-01-01〜2027-11-23）が入っている")
	void importedAll() {
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM holidays", Integer.class)).isEqualTo(1067);
		assertThat(jdbc.queryForObject("SELECT MIN(holiday_on) FROM holidays", LocalDate.class)).isEqualTo(LocalDate.of(1955, 1, 1));
		assertThat(jdbc.queryForObject("SELECT MAX(holiday_on) FROM holidays", LocalDate.class)).isEqualTo(LocalDate.of(2027, 11, 23));
		assertThat(holidayMapper.selectByPrimaryKey(LocalDate.of(1955, 1, 1)).getName()).isEqualTo("元日");
		assertThat(holidayMapper.selectByPrimaryKey(LocalDate.of(2027, 11, 23)).getName()).isEqualTo("勤労感謝の日");
	}

	@Test
	@DisplayName("祝日の判定：2026年の祝日は true、祝日でない日は false。最初・最後の日も")
	void isHoliday() {
		assertThat(holidayCalendar.isHoliday(LocalDate.of(2026, 11, 23))).isTrue();
		assertThat(holidayCalendar.isHoliday(LocalDate.of(2026, 1, 1))).isTrue();
		// 振替休日
		assertThat(holidayCalendar.isHoliday(LocalDate.of(2026, 5, 6))).isTrue();
		assertThat(holidayCalendar.isHoliday(LocalDate.of(2026, 11, 24))).isFalse();
		// 収録の最初と最後
		assertThat(holidayCalendar.isHoliday(LocalDate.of(1955, 1, 1))).isTrue();
		assertThat(holidayCalendar.isHoliday(LocalDate.of(1954, 12, 31))).isFalse();
		assertThat(holidayCalendar.isHoliday(LocalDate.of(2027, 11, 23))).isTrue();
	}

	@Test
	@DisplayName("収録の最後の日より後は、祝日にない（平日として扱う）")
	void afterLastHoliday() {
		// 2027-12-23（木）は祝日ではない。2028-01-01（土）は祝日だが、まだ入っていないので、土曜として false
		assertThat(holidayCalendar.isBusinessDay(LocalDate.of(2027, 12, 23))).isTrue();
		assertThat(holidayCalendar.isHoliday(LocalDate.of(2028, 1, 1))).isFalse();
		// 2028-01-03（月）は平日として扱う
		assertThat(holidayCalendar.isBusinessDay(LocalDate.of(2028, 1, 3))).isTrue();
	}

	@Test
	@DisplayName("平日の判定：土曜・日曜・祝日は false、ふつうの平日は true")
	void isBusinessDay() {
		assertThat(holidayCalendar.isBusinessDay(LocalDate.of(2026, 11, 16))).isTrue();
		assertThat(holidayCalendar.isBusinessDay(LocalDate.of(2026, 11, 21))).isFalse();
		assertThat(holidayCalendar.isBusinessDay(LocalDate.of(2026, 11, 22))).isFalse();
		// 2026-11-23（月）は勤労感謝の日
		assertThat(holidayCalendar.isBusinessDay(LocalDate.of(2026, 11, 23))).isFalse();
	}

	@Test
	@DisplayName("その日かそれより前の平日：平日はその日、土日はさかのぼる、祝日の月曜は前の金曜")
	void businessDayOnOrBefore() {
		assertThat(holidayCalendar.businessDayOnOrBefore(LocalDate.of(2026, 11, 20))).isEqualTo(LocalDate.of(2026, 11, 20));
		assertThat(holidayCalendar.businessDayOnOrBefore(LocalDate.of(2026, 11, 21))).isEqualTo(LocalDate.of(2026, 11, 20));
		assertThat(holidayCalendar.businessDayOnOrBefore(LocalDate.of(2026, 11, 22))).isEqualTo(LocalDate.of(2026, 11, 20));
		assertThat(holidayCalendar.businessDayOnOrBefore(LocalDate.of(2026, 11, 23))).isEqualTo(LocalDate.of(2026, 11, 20));
	}

	@Test
	@DisplayName("その日かそれより前の平日：ゴールデンウィークの続き（2026-05-06 は振替休日）は 5/1（金）まで戻る")
	void goldenWeek() {
		assertThat(holidayCalendar.businessDayOnOrBefore(LocalDate.of(2026, 5, 6))).isEqualTo(LocalDate.of(2026, 5, 1));
		assertThat(holidayCalendar.businessDayOnOrBefore(LocalDate.of(2026, 5, 7))).isEqualTo(LocalDate.of(2026, 5, 7));
	}

	@Test
	@DisplayName("催促日（実際の祝日）：2026年は、土日の月（1・4・7・10月）が前の金曜、ほかは25日")
	void reminderDates2026() {
		assertThat(reminderCalendar.reminderDate(YearMonth.of(2026, 1))).isEqualTo(LocalDate.of(2026, 1, 23));
		assertThat(reminderCalendar.reminderDate(YearMonth.of(2026, 2))).isEqualTo(LocalDate.of(2026, 2, 25));
		assertThat(reminderCalendar.reminderDate(YearMonth.of(2026, 4))).isEqualTo(LocalDate.of(2026, 4, 24));
		assertThat(reminderCalendar.reminderDate(YearMonth.of(2026, 7))).isEqualTo(LocalDate.of(2026, 7, 24));
		assertThat(reminderCalendar.reminderDate(YearMonth.of(2026, 10))).isEqualTo(LocalDate.of(2026, 10, 23));
		assertThat(reminderCalendar.reminderDate(YearMonth.of(2026, 11))).isEqualTo(LocalDate.of(2026, 11, 25));
		assertThat(reminderCalendar.reminderDate(YearMonth.of(2026, 12))).isEqualTo(LocalDate.of(2026, 12, 25));
	}

	@Test
	@DisplayName("25日が祝日のとき（祝日を足して確かめる）：前の平日。その日も祝日なら、さらに前の平日（実際の祝日 11/23 も越える）")
	void reminderDateWhenHoliday() {
		addHoliday(LocalDate.of(2026, 11, 25));
		assertThat(reminderCalendar.reminderDate(YearMonth.of(2026, 11))).isEqualTo(LocalDate.of(2026, 11, 24));

		addHoliday(LocalDate.of(2026, 11, 24));
		// 24日・25日が祝日、23日（月）も祝日、22日（日）・21日（土）→ 20日（金）
		assertThat(reminderCalendar.reminderDate(YearMonth.of(2026, 11))).isEqualTo(LocalDate.of(2026, 11, 20));
	}

	@Test
	@DisplayName("催促の日時（設定は毎朝9:00）：催促日の9:00（日本時間）")
	void reminderAt() {
		ZonedDateTime at = reminderCalendar.reminderAt(YearMonth.of(2026, 10));
		assertThat(at.toLocalDateTime()).isEqualTo(LocalDate.of(2026, 10, 23).atTime(9, 0));
		assertThat(at.getZone()).isEqualTo(TOKYO);
	}

	@Test
	@DisplayName("催促を送ってよい日か：実際の祝日で（2026-11：23日は祝日、25日が催促日）")
	void isReminderDue() {
		assertThat(reminderCalendar.isReminderDue(LocalDate.of(2026, 11, 24))).isFalse();
		assertThat(reminderCalendar.isReminderDue(LocalDate.of(2026, 11, 25))).isTrue();
		// 2026-10 は 23日（金）が催促日。24日（土）・25日（日）は送らず、26日（月）は送ってよい
		assertThat(reminderCalendar.isReminderDue(LocalDate.of(2026, 10, 22))).isFalse();
		assertThat(reminderCalendar.isReminderDue(LocalDate.of(2026, 10, 23))).isTrue();
		assertThat(reminderCalendar.isReminderDue(LocalDate.of(2026, 10, 24))).isFalse();
		assertThat(reminderCalendar.isReminderDue(LocalDate.of(2026, 10, 25))).isFalse();
		assertThat(reminderCalendar.isReminderDue(LocalDate.of(2026, 10, 26))).isTrue();
	}

	@Test
	@DisplayName("今月の催促日は、時計から決まる（組み立てた Bean の時計は日本時間）")
	void clockZone() {
		Clock fixed = Clock.fixed(ZonedDateTime.of(2026, 7, 1, 0, 30, 0, 0, TOKYO).toInstant(), TOKYO);
		ReminderCalendar calendar = new ReminderCalendar(holidayCalendar, fixed, "0 0 9 * * *");
		assertThat(calendar.reminderDateOfThisMonth()).isEqualTo(LocalDate.of(2026, 7, 24));
	}
}
