package jp.co.highjunk.equipment.common.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 組み立てた画面を配る仕組み（詳細設計 common.md 3.9）のテスト。
 * テストのときは、src/test/resources/static/index.html（id="test-index-page" の印があるもの）を画面として使う。
 */
@SpringBootTest
@AutoConfigureMockMvc
class SpaWebConfigTest {

	@Autowired
	MockMvc mockMvc;

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "/loans", "/equipment/12", "/login" })
	@DisplayName("画面のアドレスでは、ログインしていなくても index.html を返す")
	void screenPathsReturnIndex(String path) throws Exception {
		mockMvc.perform(get(path))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("test-index-page")));
	}

	@Test
	@DisplayName("トップ（/）では index.html に回す（Spring Boot の最初のページの仕組み。実際のサーバーでは index.html が返る）")
	void topForwardsToIndex() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(forwardedUrl("index.html"));
	}

	@Test
	@DisplayName("名前に「.」を含むのにファイルがないアドレスは 404")
	void missingFileIsNotFound() throws Exception {
		mockMvc.perform(get("/assets/no-such-file.js"))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	@DisplayName("/api/ で始まる存在しないアドレスは、今までどおり 404 の JSON")
	void unknownApiIsJsonNotFound() throws Exception {
		mockMvc.perform(get("/api/no-such-api"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"));
	}

	@Test
	@DisplayName("A-08 は今までどおり JSON を返す")
	void healthStillWorks() throws Exception {
		mockMvc.perform(get("/api/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.state").value("UP"));
	}
}
