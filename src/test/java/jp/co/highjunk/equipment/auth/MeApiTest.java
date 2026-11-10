package jp.co.highjunk.equipment.auth;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A-03 ログイン中の人の情報（docs/API設計.md 3.1・4.2）のテスト。
 */
@DisplayName("A-03 ログイン中の人の情報")
class MeApiTest extends AuthApiTestBase {

	@Test
	@DisplayName("ログインしていなければ 401（ログインしてください。）")
	void notLoggedIn() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("ログインしてください。"));
	}

	@Test
	@DisplayName("ログインしていれば、A-01 と同じ形で返す")
	void loggedIn() throws Exception {
		me(loginSession(IPPAN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(userId(IPPAN)))
				.andExpect(jsonPath("$.data.name").value("開発 一般"))
				.andExpect(jsonPath("$.data.email").value(IPPAN))
				.andExpect(jsonPath("$.data.roleName").value("一般社員"))
				.andExpect(jsonPath("$.data.permissions", contains("LOAN_SELF", "CONFIRM_SELF")))
				.andExpect(jsonPath("$.data.homePage").value("MY_EQUIPMENT"))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}
}
