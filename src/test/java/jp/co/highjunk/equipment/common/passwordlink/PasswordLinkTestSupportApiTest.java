package jp.co.highjunk.equipment.common.passwordlink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

/**
 * テスト用のリンクを作る・消す API（詳細設計 common.md 3.14）のテスト。
 * テストの設定（app.test-support.enabled が true）と、本番の設定（false）の両方を確かめる。
 */
class PasswordLinkTestSupportApiTest {

	private static final String URL = "/api/test-support/password-links";

	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@Transactional
	@DisplayName("テストの設定（app.test-support.enabled が true）")
	class Enabled {

		@Autowired
		MockMvc mockMvc;

		@Autowired
		JdbcTemplate jdbc;

		private String issue(String email, String purpose) throws Exception {
			MvcResult result = mockMvc.perform(post(URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"" + email + "\",\"purpose\":\"" + purpose + "\"}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value(200))
					.andExpect(jsonPath("$.data.purpose").value(purpose))
					.andExpect(jsonPath("$.data.expiresAt").isString())
					.andReturn();
			return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
		}

		@Test
		@DisplayName("再設定のリンクを作り、合言葉（43文字）を返す。保存するのはハッシュで、A-05 で使える。ログインなし・CSRFの合言葉なしで呼べる")
		void issueReset() throws Exception {
			String token = issue("ippan@example.com", "RESET");

			assertThat(token).matches("[A-Za-z0-9_-]{43}");
			assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM password_tokens WHERE token_hash = ? AND purpose = 'RESET'",
					Integer.class, PasswordLinkTokens.hash(token))).isEqualTo(1);
			mockMvc.perform(post("/api/auth/password-tokens/verify")
					.with(jp.co.highjunk.equipment.testsupport.CsrfTestHelper.csrfToken(mockMvc))
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"token\":\"" + token + "\"}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.email").value("ippan@example.com"))
					.andExpect(jsonPath("$.data.purpose").value("RESET"));
		}

		@Test
		@DisplayName("ロック・ユーザ登録の用途でも作れる。5分に1回の決まりに当たらず、何度でも作れる（前のリンクは無効になる）")
		void issueOtherPurposes() throws Exception {
			String first = issue("soumu@example.com", "RESET");
			String second = issue("soumu@example.com", "RESET");
			issue("soumu@example.com", "LOCK");
			issue("admin@example.com", "REGISTER");

			assertThat(first).isNotEqualTo(second);
			assertThat(jdbc.queryForObject(
					"SELECT COUNT(*) FROM password_tokens WHERE user_id = (SELECT id FROM users WHERE email = ?) AND revoked_at IS NULL",
					Integer.class, "soumu@example.com")).isEqualTo(1);
		}

		@Test
		@DisplayName("メールアドレスの大文字・小文字は区別しない")
		void ignoresCase() throws Exception {
			issue("Ippan@Example.COM", "RESET");
		}

		@Test
		@DisplayName("登録のないメールアドレス・削除されたユーザ：404")
		void unknownOrDeleted() throws Exception {
			mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"nobody@example.com\",\"purpose\":\"RESET\"}"))
					.andExpect(status().isNotFound());
			jdbc.update("UPDATE users SET deleted_at = '2026-11-08 10:00:00' WHERE email = 'ippan@example.com'");
			mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"ippan@example.com\",\"purpose\":\"RESET\"}"))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("用途がない・知らない値、メールアドレスが空：400")
		void badRequest() throws Exception {
			for (String json : new String[] { "{\"email\":\"ippan@example.com\"}",
					"{\"email\":\"ippan@example.com\",\"purpose\":\"OTHER\"}", "{\"email\":\"\",\"purpose\":\"RESET\"}",
					"{\"purpose\":\"RESET\"}" }) {
				mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
						.andExpect(status().isBadRequest());
			}
		}

		@Test
		@DisplayName("DELETE で、リンクが全部消える。ログインなし・CSRFの合言葉なしで呼べる")
		void clear() throws Exception {
			issue("ippan@example.com", "RESET");
			issue("soumu@example.com", "RESET");

			mockMvc.perform(delete(URL))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data").doesNotExist());

			assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM password_tokens", Integer.class)).isZero();
		}
	}

	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@TestPropertySource(properties = "app.test-support.enabled=false")
	@DisplayName("本番の設定（app.test-support.enabled が false）")
	class Disabled {

		@Autowired
		MockMvc mockMvc;

		@Autowired
		ApplicationContext context;

		@Test
		@DisplayName("controller と Service の Bean がない")
		void noBeans() {
			assertThat(context.getBeansOfType(PasswordLinkTestSupportController.class)).isEmpty();
			assertThat(context.getBeansOfType(PasswordLinkTestSupportService.class)).isEmpty();
		}

		@Test
		@DisplayName("POST・DELETE とも 404（ログインなしでも、401 ではなく 404）")
		void notFound() throws Exception {
			mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"ippan@example.com\",\"purpose\":\"RESET\"}"))
					.andExpect(status().isNotFound());
			mockMvc.perform(delete(URL)).andExpect(status().isNotFound());
		}
	}
}
