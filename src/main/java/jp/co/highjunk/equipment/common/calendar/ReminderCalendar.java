package jp.co.highjunk.equipment.common.calendar;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.config.AppProperties;

/**
 * 催促日・催促の日時の計算（機能一覧 7章、詳細設計 common.md 3.18）。
 * 催促日＝毎月25日。土曜・日曜・祝日なら、前の平日（その日も祝日なら、さらに前の平日）。
 * 催促の時刻は、設定 app.reminder-cron の時刻（毎日1回動く形のときだけ。そうでなければ 9:00）。
 */
@Component
public class ReminderCalendar {

	/** 催促日のもとになる日（毎月25日。機能一覧 7章の1） */
	static final int REMINDER_DAY_OF_MONTH = 25;

	/** cron から時刻を読み取れないときの時刻（画面設計・API設計の「9:00」） */
	static final LocalTime DEFAULT_TIME = LocalTime.of(9, 0);

	/** 毎日1回動く形：「秒 分 時 * * *」（日・月・曜日は * か ?） */
	private static final Pattern DAILY_CRON = Pattern.compile("^(\\d{1,2})\\s+(\\d{1,2})\\s+(\\d{1,2})\\s+[*?]\\s+[*?]\\s+[*?]$");

	private final HolidayCalendar holidayCalendar;

	private final Clock clock;

	private final LocalTime reminderTime;

	@Autowired
	public ReminderCalendar(HolidayCalendar holidayCalendar, Clock clock, AppProperties appProperties) {
		this(holidayCalendar, clock, appProperties.reminderCron());
	}

	/**
	 * @param reminderCron 催促の処理を動かす cron。書き方が誤っていれば、起動のときに IllegalArgumentException で止める
	 */
	public ReminderCalendar(HolidayCalendar holidayCalendar, Clock clock, String reminderCron) {
		this.holidayCalendar = holidayCalendar;
		this.clock = clock;
		CronExpression.parse(reminderCron.trim());
		this.reminderTime = timeOf(reminderCron.trim());
	}

	private static LocalTime timeOf(String cron) {
		Matcher matcher = DAILY_CRON.matcher(cron);
		if (!matcher.matches()) {
			return DEFAULT_TIME;
		}
		int second = Integer.parseInt(matcher.group(1));
		int minute = Integer.parseInt(matcher.group(2));
		int hour = Integer.parseInt(matcher.group(3));
		return LocalTime.of(hour, minute, second);
	}

	/** その月の催促日 */
	public LocalDate reminderDate(YearMonth month) {
		return holidayCalendar.businessDayOnOrBefore(month.atDay(REMINDER_DAY_OF_MONTH));
	}

	/** その月の催促の日時（催促日の、催促の時刻。時計の時間帯＝日本時間） */
	public ZonedDateTime reminderAt(YearMonth month) {
		return ZonedDateTime.of(reminderDate(month), reminderTime, clock.getZone());
	}

	/** 今月の催促日 */
	public LocalDate reminderDateOfThisMonth() {
		return reminderDate(YearMonth.now(clock));
	}

	/** 今月の催促の日時（B-01 の reminderAt） */
	public ZonedDateTime reminderAtOfThisMonth() {
		return reminderAt(YearMonth.now(clock));
	}

	/**
	 * 催促を送ってよい日か（基本設計書 7章の3の1・2）。
	 * 今日が平日で、今日がその月の催促日か、それより後。「その月の催促をまだ送っていない」は、呼ぶ側が reminder_runs で確かめる。
	 */
	public boolean isReminderDue(LocalDate today) {
		if (!holidayCalendar.isBusinessDay(today)) {
			return false;
		}
		return !today.isBefore(reminderDate(YearMonth.from(today)));
	}
}
