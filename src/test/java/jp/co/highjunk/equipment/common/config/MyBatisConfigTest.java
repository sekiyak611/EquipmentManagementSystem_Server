package jp.co.highjunk.equipment.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * mapper の見つけ方（common.config.MyBatisConfig。詳細設計 common.md 4章）のテスト。
 * mapper の名前は `<テーブルの名前>Mapper` なので、同じ名前の mapper がパッケージ違いでできる（CLAUDE.md 5章）。
 */
@SpringBootTest
@DisplayName("mapper の部品の名前（MyBatisConfig）")
class MyBatisConfigTest {

	@Autowired
	ApplicationContext context;

	@Autowired
	jp.co.highjunk.equipment.mapper.namecheck.first.SameNameMapper firstMapper;

	@Autowired
	jp.co.highjunk.equipment.mapper.namecheck.second.SameNameMapper secondMapper;

	@Test
	@DisplayName("同じ名前の mapper がパッケージ違いで2つあっても、起動でき、それぞれの SQL が動く")
	void sameNameMappersInDifferentPackages() {
		assertThat(firstMapper.selectNumber()).isEqualTo(1);
		assertThat(secondMapper.selectNumber()).isEqualTo(2);
	}

	@Test
	@DisplayName("mapper の部品の名前は、パッケージ込みの名前になる")
	void beanNameIsFullyQualified() {
		assertThat(context.containsBean("jp.co.highjunk.equipment.mapper.namecheck.first.SameNameMapper")).isTrue();
		assertThat(context.containsBean("jp.co.highjunk.equipment.mapper.namecheck.second.SameNameMapper")).isTrue();
		assertThat(context.containsBean("jp.co.highjunk.equipment.mapper.auth.UserMapper")).isTrue();
		assertThat(context.containsBean("sameNameMapper")).isFalse();
	}
}
