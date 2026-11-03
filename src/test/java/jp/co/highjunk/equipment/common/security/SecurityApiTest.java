package jp.co.highjunk.equipment.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

/**
 * ログインと権限の確かめ方の土台（docs/API設計.md 2.2）のテスト。
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityApiTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ApplicationContext context;

	@Test
	@DisplayName("Spring Boot の仮のユーザを作らない（作ると、仮のパスワードがログに出るため）")
	void noGeneratedUser() {
		assertThat(context.getBeansOfType(UserDetailsService.class)).isEmpty();
	}

	@Test
	@DisplayName("ログインしていないのに、ログインが必要なAPIを呼ぶと 401。ログイン画面に移さず、2.4の形で返す")
	void unauthorized() throws Exception {
		mockMvc.perform(get("/api/me/loans"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("ログインしてください。"))
				.andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.errors").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	@DisplayName("CSRFの合言葉を付けずにPOSTすると 403")
	void postWithoutCsrfToken() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value("この操作を行う権限がありません。"));
	}

	@Test
	@DisplayName("合言葉は XSRF-TOKEN クッキーで渡され、X-XSRF-TOKEN 見出しで送り返すと通る")
	void csrfTokenRoundTrip() throws Exception {
		MvcResult first = mockMvc.perform(get("/api/health")).andReturn();
		Cookie xsrf = first.getResponse().getCookie("XSRF-TOKEN");
		assertThat(xsrf).as("XSRF-TOKEN クッキーが渡される").isNotNull();
		assertThat(xsrf.isHttpOnly()).as("画面のプログラムから読めるよう HttpOnly にしない").isFalse();

		// A-01（ログイン）は ZPOP-25 で作るので、今は「見つからない（404）」になる。
		// 合言葉が通らなければ 403 になるので、403 にならないことを確かめる
		mockMvc.perform(post("/api/auth/login").cookie(xsrf).header("X-XSRF-TOKEN", xsrf.getValue())
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("合言葉が違うと 403")
	void wrongCsrfToken() throws Exception {
		MvcResult first = mockMvc.perform(get("/api/health")).andReturn();
		Cookie xsrf = first.getResponse().getCookie("XSRF-TOKEN");
		mockMvc.perform(post("/api/auth/login").cookie(xsrf).header("X-XSRF-TOKEN", "wrong-token")
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	@DisplayName("ログインしている人のユーザIDがログに付き、合言葉のクッキーも同じ処理で渡される（2つの部品が両方動く）")
	void userIdAndCsrfCookieBothWork() throws Exception {
		LoginUserId loginUser = () -> 12L;
		MvcResult result = mockMvc.perform(get("/api/test-security/log-user-id")
				.with(authentication(new UsernamePasswordAuthenticationToken(loginUser, null, List.of()))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value("12"))
				.andReturn();
		assertThat(result.getResponse().getCookie("XSRF-TOKEN")).isNotNull();
	}
}
