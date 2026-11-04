package jp.co.highjunk.equipment.health;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.highjunk.equipment.mapper.health.HealthMapper;

/**
 * A-08 アプリとデータベースが動いているかを答える（docs/API設計.md 3.1）のテスト。
 */
class HealthApiTest {

	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@DisplayName("データベースにつながるとき（テスト専用のデータベース equipment_test を使う）")
	class WhenDatabaseIsUp {

		@Autowired
		MockMvc mockMvc;

		@Test
		@DisplayName("ログインしていなくても呼べて、200 と state=UP を返す")
		void up() throws Exception {
			mockMvc.perform(get("/api/health"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value(200))
					.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
					.andExpect(jsonPath("$.data.state").value("UP"))
					.andExpect(jsonPath("$.errors").value(nullValue()));
		}

		@Test
		@DisplayName("版や設定などの詳しい情報は返さない（data は state だけ）")
		void noDetails() throws Exception {
			mockMvc.perform(get("/api/health"))
					.andExpect(jsonPath("$.data.length()").value(1));
		}
	}

	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@DisplayName("データベースにつながらないとき（データベースの読み書きを偽物にして、つながらない状態を作る）")
	class WhenDatabaseIsDown {

		@Autowired
		MockMvc mockMvc;

		@MockitoBean
		HealthMapper healthMapper;

		@Test
		@DisplayName("503 を返す。詳しい理由は返さない")
		void down() throws Exception {
			given(healthMapper.selectOne()).willThrow(new CannotGetJdbcConnectionException("接続できない"));

			mockMvc.perform(get("/api/health"))
					.andExpect(status().isServiceUnavailable())
					.andExpect(jsonPath("$.status").value(503))
					.andExpect(jsonPath("$.message").value("現在利用できません。"))
					.andExpect(jsonPath("$.data").value(nullValue()))
					.andExpect(jsonPath("$.errors").value(nullValue()));
		}
	}
}
