package jp.co.highjunk.equipment.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jp.co.highjunk.equipment.common.error.ApiResponseWriter;
import tools.jackson.databind.json.JsonMapper;

/**
 * APIを呼ぶたびに権限項目を読み直すフィルター（詳細設計 common.md 3.10）の単体テスト。
 * データベースを使わず、読み直しの部品（LoginUserRefresher）を決まった答えを返すものに置き換える。
 */
class LoginRefreshFilterTest {

	private final ApiResponseWriter writer = new ApiResponseWriter(JsonMapper.builder().build());

	@AfterEach
	void clear() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("データベースを読めなければ、500 を API設計 2.4 の形で返し、先に進めない")
	void databaseError() throws Exception {
		LoginUserRefresher failing = user -> {
			throw new DataAccessResourceFailureException("つながらない");
		};
		login(List.of("EQUIPMENT_VIEW"));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		new LoginRefreshFilter(failing, writer).doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(500);
		assertThat(response.getContentAsString()).contains("\"status\":500")
				.contains("問題が発生しました。時間をおいて、もう一度お試しください。続く場合は総務に連絡してください。")
				.contains("\"data\":null");
		assertThat(chain.getRequest()).as("controller まで進まない").isNull();
	}

	@Test
	@DisplayName("画面のファイル（/api/ で始まらないアドレス）では、読み直さない")
	void skipsNonApi() throws Exception {
		LoginUserRefresher failing = user -> {
			throw new AssertionError("呼ばれてはいけない");
		};
		login(List.of());
		MockFilterChain chain = new MockFilterChain();

		new LoginRefreshFilter(failing, writer).doFilter(new MockHttpServletRequest("GET", "/assets/index.js"),
				new MockHttpServletResponse(), chain);

		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	@DisplayName("読み直した権限項目で、そのリクエストのログイン状態を作り直す")
	void replacesAuthorities() throws Exception {
		login(List.of("LOAN_SELF"));
		MockFilterChain chain = new MockFilterChain();

		new LoginRefreshFilter(user -> Optional.of(List.of("EQUIPMENT_VIEW")), writer)
				.doFilter(new MockHttpServletRequest("GET", "/api/auth/me"), new MockHttpServletResponse(), chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
				.extracting(Object::toString).containsExactly("EQUIPMENT_VIEW");
	}

	private void login(List<String> permissions) {
		SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
				new LoginUser(1L, LocalDateTime.of(2026, 11, 8, 10, 0)), null,
				permissions.stream().map(p -> (org.springframework.security.core.GrantedAuthority) () -> p).toList()));
	}
}
