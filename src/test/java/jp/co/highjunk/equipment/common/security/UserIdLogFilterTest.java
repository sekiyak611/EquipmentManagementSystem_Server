package jp.co.highjunk.equipment.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * ログに操作した人のユーザIDを付ける部品の単体テスト。
 */
class UserIdLogFilterTest {

	private final UserIdLogFilter filter = new UserIdLogFilter();

	@AfterEach
	void clear() {
		SecurityContextHolder.clearContext();
		MDC.clear();
	}

	@Test
	@DisplayName("ログインしている人のユーザIDが、処理の間だけログに付き、終わったら消える")
	void putsUserIdDuringRequest() throws Exception {
		LoginUserId loginUser = () -> 12L;
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
		AtomicReference<String> during = new AtomicReference<>();

		filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
				(req, res) -> during.set(MDC.get("userId")));

		assertThat(during.get()).isEqualTo("12");
		assertThat(MDC.get("userId")).isNull();
	}

	@Test
	@DisplayName("ログインしていないときは、ユーザIDを付けない")
	void noUserIdWhenNotLoggedIn() throws Exception {
		AtomicReference<String> during = new AtomicReference<>("not-called");

		filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
				(req, res) -> during.set(MDC.get("userId")));

		assertThat(during.get()).isNull();
	}
}
