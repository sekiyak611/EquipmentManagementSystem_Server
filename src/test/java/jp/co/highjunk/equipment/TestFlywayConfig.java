package jp.co.highjunk.equipment;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * テストを始める前に、テスト専用のデータベース（equipment_test）のテーブルを作り直す（docs/テスト方針.md 4章）。
 * テストのときだけ使う（src/test に置き、test の設定のときだけ動く）。
 */
@Configuration(proxyBeanMethods = false)
@Profile("test")
public class TestFlywayConfig {

	@Bean
	public FlywayMigrationStrategy cleanThenMigrate() {
		return flyway -> {
			flyway.clean();
			flyway.migrate();
		};
	}
}
