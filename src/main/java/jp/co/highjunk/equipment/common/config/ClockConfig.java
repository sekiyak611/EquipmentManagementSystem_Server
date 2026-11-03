package jp.co.highjunk.equipment.common.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 時計（java.time.Clock）を1つ用意する。
 * 今の日時は、LocalDate.now() などで直接取らず、この時計から取る（CLAUDE.md 5章）。
 * テストでは、好きな日時に止めた時計に差し替える。
 */
@Configuration(proxyBeanMethods = false)
public class ClockConfig {

	@Bean
	public Clock clock(AppProperties appProperties) {
		return Clock.system(appProperties.timeZone());
	}
}
