package jp.co.highjunk.equipment.common.security;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ログの1行ごとに、操作した人のユーザIDを付ける部品（docs/API設計.md 7章）。
 * リクエストの間だけ、ログの「付け足し情報」（MDC）に userId を入れ、終わったら消す。
 * ログインしていないときは何も入れない（ログには「-」と出る）。
 */
class UserIdLogFilter extends OncePerRequestFilter {

	/** logback-spring.xml の %X{userId} と同じ名前 */
	static final String MDC_KEY = "userId";

	/**
	 * 「この処理はもう済んだ」の印の名前。
	 * Springの部品として登録していないフィルターは名前がないため、そのままだと印の名前がほかのフィルターとぶつかる。
	 * ぶつかると、どちらかが動かなくなるので、クラスの名前を使う。
	 */
	@Override
	protected String getAlreadyFilteredAttributeName() {
		return getClass().getName() + ".FILTERED";
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof LoginUserId loginUser) {
			MDC.put(MDC_KEY, String.valueOf(loginUser.getUserId()));
		}
		try {
			chain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}
}
