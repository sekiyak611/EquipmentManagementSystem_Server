package jp.co.highjunk.equipment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

/**
 * 開発用のユーザ（db/devdata。詳細設計 common.md 4章）が、本番（prod）の設定では当たらないことのテスト。
 * 設定ファイルを読んで、Flyway の SQL の置き場所を確かめる。
 */
class DevDataProfileTest {

	@Test
	@DisplayName("どの環境でも同じ設定（application.yml）と本番の設定（application-prod.yml）には、db/devdata が入っていない")
	void prodDoesNotUseDevData() throws Exception {
		assertThat(locations("application.yml")).isEqualTo("classpath:db/migration");
		assertThat(locations("application-prod.yml")).as("prod では置き場所を上書きしない（application.yml のまま）").isNull();
	}

	@Test
	@DisplayName("開発（dev）とテスト（test）の設定だけが、db/devdata を足している")
	void devAndTestUseDevData() throws Exception {
		assertThat(locations("application-dev.yml")).isEqualTo("classpath:db/migration,classpath:db/devdata");
		assertThat(locations("application-test.yml")).isEqualTo("classpath:db/migration,classpath:db/devdata");
	}

	private static Object locations(String file) throws Exception {
		List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(file, new ClassPathResource(file));
		return sources.stream().map(s -> s.getProperty("spring.flyway.locations")).filter(v -> v != null)
				.findFirst().map(Object::toString).orElse(null);
	}
}
