package jp.co.highjunk.equipment.common.security;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * セッションが切れていても、「ログインしたまま」のクッキーがあれば、自動でログインし直すフィルター
 * （詳細設計 common.md 3.17）。LoginRefreshFilter の前に置く（作ったログイン状態の権限項目を、あとの LoginRefreshFilter が読み直す）。
 * <ul>
 * <li>画面のファイル（/api/ で始まらないアドレス）では、データベースを読まない（LoginRefreshFilter と同じ）。</li>
 * <li>すでにログイン状態があるときは、何もしない。</li>
 * <li>自動ログインできたら、ログイン状態を LoginUser（ログインした日時は今）にして、セッションにも保存する。次からはセッションで続く。</li>
 * </ul>
 */
class RememberMeLoginFilter extends OncePerRequestFilter {

	private final LoginRememberMe rememberMe;

	private final SecurityContextRepository securityContextRepository;

	private final Clock clock;

	RememberMeLoginFilter(LoginRememberMe rememberMe, SecurityContextRepository securityContextRepository,
			Clock clock) {
		this.rememberMe = rememberMe;
		this.securityContextRepository = securityContextRepository;
		this.clock = clock;
	}

	/** ほかのフィルターと「この処理はもう済んだ」の印の名前がぶつからないよう、クラスの名前を使う（LoginRefreshFilter と同じ）。 */
	@Override
	protected String getAlreadyFilteredAttributeName() {
		return getClass().getName() + ".FILTERED";
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith(request.getContextPath() + "/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (!isLoggedIn() && rememberMe.isRemembered(request)) {
			Optional<Long> userId = rememberMe.autoLogin(request, response);
			if (userId.isPresent()) {
				LoginUser loginUser = new LoginUser(userId.get(), LocalDateTime.now(clock));
				SecurityContext context = SecurityContextHolder.createEmptyContext();
				// 権限項目は、あとの LoginRefreshFilter が読み直す
				context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(loginUser, null, List.of()));
				SecurityContextHolder.setContext(context);
				securityContextRepository.saveContext(context, request, response);
			}
		}
		chain.doFilter(request, response);
	}

	private boolean isLoggedIn() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken);
	}
}
