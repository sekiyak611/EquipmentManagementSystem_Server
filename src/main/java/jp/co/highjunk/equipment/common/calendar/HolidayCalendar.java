package jp.co.highjunk.equipment.common.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.dao.mapper.HolidayMapper;

/**
 * 祝日・平日の判定（詳細設計 common.md 3.18）。
 * 祝日は holidays テーブル（Flyway の V3 以降で入れる。内閣府の CSV）から読む。
 * 収録の最後の日より後の祝日は holidays にないので、平日として扱う（新しい年の分を足し忘れても、エラーにしない）。
 */
@Component
public class HolidayCalendar {

	private final HolidayMapper holidayMapper;

	public HolidayCalendar(HolidayMapper holidayMapper) {
		this.holidayMapper = holidayMapper;
	}

	/** holidays にある日（国民の祝日・休日）か */
	public boolean isHoliday(LocalDate date) {
		return holidayMapper.selectByPrimaryKey(date) != null;
	}

	/** 平日か（土曜・日曜・祝日のどれでもない） */
	public boolean isBusinessDay(LocalDate date) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
			return false;
		}
		return !isHoliday(date);
	}

	/** その日が平日ならその日、そうでなければ、さかのぼって最初の平日 */
	public LocalDate businessDayOnOrBefore(LocalDate date) {
		LocalDate day = date;
		while (!isBusinessDay(day)) {
			day = day.minusDays(1);
		}
		return day;
	}
}
