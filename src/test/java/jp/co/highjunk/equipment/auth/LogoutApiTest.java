package jp.co.highjunk.equipment.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * A-02 ログアウト（docs/API設計.md 3.1）のテスト。
 */
@DisplayName("A-02 ログアウト")
class LogoutApiTest extends AuthApiTestBase {

	@Test
	@DisplayName("ログアウトすると 200（data は null）。そのあとは 401")
	void logout() throws Exception {
		MockHttpSession session = loginSession(SOUMU);
		Cookie oldToken = mockMvc.perform(get("/api/health")).andReturn().getResponse().getCookie("XSRF-TOKEN");

		MvcResult result = mockMvc.perform(post("/api/auth/logout").session(session)
				// ログアウトの前の合言葉（oldToken）で送る
				.cookie(oldToken).header("X-XSRF-TOKEN", oldToken.getValue()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()))
				.andReturn();

		assertThat(session.isInvalid()).isTrue();
		Cookie newToken = result.getResponse().getCookie("XSRF-TOKEN");
		assertThat(newToken).isNotNull();
		assertThat(newToken.getValue()).isNotEmpty().isNotEqualTo(oldToken.getValue());
		me(session).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("ログインしていなければ 401")
	void notLoggedIn() throws Exception {
		mockMvc.perform(post("/api/auth/logout").with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("ログインしてください。"));
	}

	@Test
	@DisplayName("CSRF の合言葉がなければ 403。ログアウトしない")
	void withoutCsrf() throws Exception {
		MockHttpSession session = loginSession(SOUMU);

		mockMvc.perform(post("/api/auth/logout").session(session))
				.andExpect(status().isForbidden());
		me(session).andExpect(status().isOk());
	}
}
