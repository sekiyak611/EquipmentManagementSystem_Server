package jp.co.highjunk.equipment.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.unit.DataSize;

import tools.jackson.databind.json.JsonMapper;

/**
 * 設定値（docs/API設計.md 6章）と、時計・日時の形（docs/API設計.md 2.1）のテスト。
 */
@SpringBootTest
class AppPropertiesTest {

	@Autowired
	AppProperties appProperties;

	@Autowired
	Clock clock;

	@Autowired
	JsonMapper jsonMapper;

	@Test
	@DisplayName("設定値が docs/API設計.md 6章のとおりに読み込まれる")
	void settingsAreLoaded() {
		assertThat(appProperties.timeZone()).isEqualTo(ZoneId.of("Asia/Tokyo"));
		assertThat(appProperties.reminderCron()).isEqualTo("0 0 9 * * *");
		assertThat(appProperties.rememberMeValidity()).isEqualTo(Duration.ofDays(60));
		assertThat(appProperties.passwordLink().resetValidity()).isEqualTo(Duration.ofMinutes(30));
		assertThat(appProperties.passwordLink().lockValidity()).isEqualTo(Duration.ofMinutes(30));
		assertThat(appProperties.passwordLink().registrationValidity()).isEqualTo(Duration.ofDays(3));
		assertThat(appProperties.passwordLink().requestInterval()).isEqualTo(Duration.ofMinutes(5));
		assertThat(appProperties.lockThreshold()).isEqualTo(5);
		assertThat(appProperties.csvMaxSize()).isEqualTo(DataSize.ofMegabytes(1));
	}

	@Test
	@DisplayName("テストの設定では、テスト用の切り替えが有効になる")
	void testSupportIsEnabledInTestProfile() {
		assertThat(appProperties.testSupport().enabled()).isTrue();
	}

	@Test
	@DisplayName("記録に出ても、秘密の値は見えない")
	void secretsAreHiddenInToString() {
		AppProperties withSecrets = new AppProperties(appProperties.baseUrl(), appProperties.timeZone(),
				appProperties.reminderCron(), appProperties.rememberMeValidity(), appProperties.passwordLink(),
				appProperties.lockThreshold(), appProperties.csvMaxSize(), new AppProperties.Slack("xoxb-secret-token"),
				"admin@example.com", appProperties.testSupport());
		assertThat(withSecrets.toString()).doesNotContain("xoxb-secret-token").doesNotContain("admin@example.com");
	}

	@Test
	@DisplayName("時計は日本時間")
	void clockIsJapanTime() {
		assertThat(clock.getZone()).isEqualTo(ZoneId.of("Asia/Tokyo"));
	}

	@Test
	@DisplayName("日付は 2026-11-02、日時は 2026-11-25T09:00:00+09:00 の形で返す")
	void dateFormat() {
		assertThat(jsonMapper.writeValueAsString(LocalDate.of(2026, 11, 2))).isEqualTo("\"2026-11-02\"");
		OffsetDateTime reminderAt = OffsetDateTime.of(2026, 11, 25, 9, 0, 0, 0, ZoneOffset.ofHours(9));
		assertThat(jsonMapper.writeValueAsString(reminderAt)).isEqualTo("\"2026-11-25T09:00:00+09:00\"");
	}
}
