package jp.co.highjunk.equipment.common.security;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * なりすまし対策の合言葉（CSRFトークン）を、どのリクエストでもクッキー（XSRF-TOKEN）で渡すための部品。
 * Spring Security は、合言葉が使われるまでクッキーを作らないので、ここで一度読んで作らせる。
 * こうすると、ログイン（A-01）の前に画面を開いただけで、画面は合言葉を受け取れる。
 */
class CsrfCookieFilter extends OncePerRequestFilter {

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
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		if (csrfToken != null) {
			// 値を読むと、クッキーが作られる
			csrfToken.getToken();
		}
		chain.doFilter(request, response);
	}
}
