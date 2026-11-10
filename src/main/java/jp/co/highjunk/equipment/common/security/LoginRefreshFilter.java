package jp.co.highjunk.equipment.common.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jp.co.highjunk.equipment.common.error.ApiMessages;
import jp.co.highjunk.equipment.common.error.ApiResponseWriter;

/**
 * APIを呼ぶたびに、ログインしている人の権限項目を読み直すフィルター（docs/API設計.md 2.2、詳細設計 common.md 3.10）。
 * <ul>
 * <li>権限の変更が、ログインし直さなくても次の操作から効くようにする（機能一覧 8章の7）。</li>
 * <li>ユーザが削除された、ログインのあとにパスワードが変わった（session_valid_after）ときは、セッションを捨てて、
 * ログインしていない扱いにする（ログインが要る API は 401 になる）。</li>
 * </ul>
 * 作り直したログイン状態は、そのリクエストの間だけ使う（セッションの中身は変えない）。
 * データベースを読めなかったときは、controller の外なので GlobalExceptionHandler を通らない。
 * そのため、ここで 500 を docs/API設計.md 2.4 の形で返す。
 */
class LoginRefreshFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(LoginRefreshFilter.class);

	private final LoginUserRefresher refresher;

	private final ApiResponseWriter writer;

	LoginRefreshFilter(LoginUserRefresher refresher, ApiResponseWriter writer) {
		this.refresher = refresher;
		this.writer = writer;
	}

	/** ほかのフィルターと「この処理はもう済んだ」の印の名前がぶつからないよう、クラスの名前を使う（UserIdLogFilter と同じ）。 */
	@Override
	protected String getAlreadyFilteredAttributeName() {
		return getClass().getName() + ".FILTERED";
	}

	/** 画面のファイル（/api/ で始まらないアドレス）では、データベースを読まない。 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith(request.getContextPath() + "/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
			Optional<List<String>> permissions;
			try {
				permissions = refresher.currentPermissions(loginUser);
			} catch (DataAccessException e) {
				log.error("ログインしている人の権限項目を読めませんでした", e);
				writer.writeError(response, HttpStatus.INTERNAL_SERVER_ERROR, ApiMessages.INTERNAL_ERROR);
				return;
			}
			if (permissions.isEmpty()) {
				// ログアウトさせる。ここでログイン状態を消すと、あとのフィルターがログインしていない扱いにする
				HttpSession session = request.getSession(false);
				if (session != null) {
					session.invalidate();
				}
				SecurityContextHolder.clearContext();
			} else {
				SecurityContext context = SecurityContextHolder.createEmptyContext();
				context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(loginUser, null,
						AuthorityUtils.createAuthorityList(permissions.get())));
				SecurityContextHolder.setContext(context);
			}
		}
		chain.doFilter(request, response);
	}
}
