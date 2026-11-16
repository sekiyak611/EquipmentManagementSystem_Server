package jp.co.highjunk.equipment.common.security;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * ログインを始める・終える（詳細設計 common.md 3.10）。
 * メールアドレスとパスワードの確かめは auth 機能が行い、ここではログイン状態をセッションに入れる・消すだけを行う。
 */
@Component
public class LoginSession {

	private static final Logger log = LoggerFactory.getLogger(LoginSession.class);

	private final SecurityContextRepository securityContextRepository;

	private final CsrfTokenRepository csrfTokenRepository;

	private final Clock clock;

	/** 「ログインしたままにする」（60日間。詳細設計 common.md 3.17） */
	private final LoginRememberMe rememberMe;

	/** すでにセッションがあれば、その番号を変える（セッション固定攻撃を防ぐ） */
	private final ChangeSessionIdAuthenticationStrategy changeSessionId = new ChangeSessionIdAuthenticationStrategy();

	public LoginSession(SecurityContextRepository securityContextRepository, CsrfTokenRepository csrfTokenRepository,
			Clock clock, LoginRememberMe rememberMe) {
		this.securityContextRepository = securityContextRepository;
		this.csrfTokenRepository = csrfTokenRepository;
		this.clock = clock;
		this.rememberMe = rememberMe;
	}

	/**
	 * ログインを始める。セッションの番号を変え、ログイン状態をセッションに保存し、なりすまし対策の合言葉を作り直す。
	 *
	 * @param userId      ログインした人のユーザID
	 * @param permissions ログインした人の権限項目のコード
	 * @param rememberMe  true なら、この端末を「ログインしたまま」（60日間）にする。false なら、この端末に前の「ログインしたまま」が
	 *                    あれば、消す（common.md 3.17 の判断 No.2）
	 */
	public void start(long userId, Collection<String> permissions, boolean rememberMe, HttpServletRequest request,
			HttpServletResponse response) {
		LoginUser loginUser = new LoginUser(userId, LocalDateTime.now(clock));
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(loginUser, null,
				AuthorityUtils.createAuthorityList(permissions));
		changeSessionId.onAuthentication(authentication, request, response);

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);

		renewCsrfToken(request, response);

		if (rememberMe) {
			this.rememberMe.remember(userId, request, response);
		} else {
			this.rememberMe.forget(request, response);
		}
	}

	/** この端末に、「ログインしたまま」のクッキーがあるか（A-07 が、パスワードを変えたあと、この端末の分を作り直すかを決めるため）。 */
	public boolean isRemembered(HttpServletRequest request) {
		return rememberMe.isRemembered(request);
	}

	/**
	 * ログインを終える。セッションを捨て、ログイン状態を消し、なりすまし対策の合言葉を作り直す。
	 * この端末の「ログインしたまま」（クッキーと、その番号の行）も消す。ほかの端末の分は残る。
	 */
	public void end(HttpServletRequest request, HttpServletResponse response) {
		log.info("ログアウトしました");
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		SecurityContextHolder.clearContext();
		renewCsrfToken(request, response);
		rememberMe.forget(request, response);
	}

	/**
	 * なりすまし対策の合言葉（XSRF-TOKEN のクッキー）を作り直す。
	 * ログインの前後で同じ合言葉を使わせないため（Spring Security の通常のログインと同じ動き）。
	 */
	private void renewCsrfToken(HttpServletRequest request, HttpServletResponse response) {
		CsrfToken token = csrfTokenRepository.generateToken(request);
		csrfTokenRepository.saveToken(token, request, response);
	}
}
