package jp.co.highjunk.equipment.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import jp.co.highjunk.equipment.common.error.ApiMessages;
import jp.co.highjunk.equipment.common.error.ApiResponseWriter;

/**
 * ログインと権限の確かめ方の土台（docs/API設計.md 2.2、docs/基本設計書.md 5章）。
 * メールアドレスとパスワードの確かめ（A-01）は auth 機能が行う。ログイン状態の持ち方は詳細設計 common.md 3.10。
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

	/** ログインしていなくても呼べるAPI（POST）。A-01、A-04〜A-06。 */
	private static final String[] PUBLIC_POST_APIS = {
			"/api/auth/login",
			"/api/auth/password-reset-requests",
			"/api/auth/password-tokens/verify",
			"/api/auth/password-tokens/use",
	};

	/** ログインしていなくても呼べるAPI（GET）。A-08。 */
	private static final String[] PUBLIC_GET_APIS = {
			"/api/health",
	};

	/** パスワードの変換と照らし合わせ。bcrypt（元に戻せない形に変換する計算方法。docs/基本設計書.md 5章） */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/** ログイン状態をセッションに入れる・読む部品。LoginSession（ログインを始める）と同じものを使う */
	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	/**
	 * なりすまし対策の合言葉を、XSRF-TOKEN のクッキーで渡す部品（docs/API設計.md 2.2）。
	 * 画面のプログラムが読めるよう、HttpOnly にしない。LoginSession（合言葉を作り直す）と同じものを使う
	 */
	@Bean
	public CsrfTokenRepository csrfTokenRepository() {
		return CookieCsrfTokenRepository.withHttpOnlyFalse();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiResponseWriter writer,
			SecurityContextRepository securityContextRepository, CsrfTokenRepository csrfTokenRepository,
			LoginUserRefresher loginUserRefresher) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, PUBLIC_POST_APIS).permitAll()
						.requestMatchers(HttpMethod.GET, PUBLIC_GET_APIS).permitAll()
						// ほかの API はログインが必要。権限項目は各APIの @PreAuthorize で確かめる（CLAUDE.md 5章）
						.requestMatchers("/api/**").authenticated()
						// 画面のファイル（React）はだれでも受け取れる（ZPOP-22 で配る）
						.anyRequest().permitAll())
				// なりすまし対策（CSRF）。サーバーは XSRF-TOKEN というクッキーで合言葉を渡し、
				// 画面は X-XSRF-TOKEN という見出しに入れて送り返す（画面（React）向けの設定）
				.csrf(csrf -> csrf.spa().csrfTokenRepository(csrfTokenRepository))
				// ログイン状態はセッションに入れる（LoginSession が保存する）
				.securityContext(context -> context.securityContextRepository(securityContextRepository))
				// APIを呼ぶたびに、ログインしている人の権限項目を読み直す（3.10）
				.addFilterAfter(new LoginRefreshFilter(loginUserRefresher, writer), SecurityContextHolderFilter.class)
				// 最初の画面表示のときから、合言葉のクッキーを渡しておく
				.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
				// ログに、操作した人のユーザIDを付ける
				.addFilterAfter(new UserIdLogFilter(), AnonymousAuthenticationFilter.class)
				// ログインしていない（401）・権限がない（403）ときは、ログイン画面に移さず、2.4の形のJSONで返す
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, e) ->
								writer.writeError(response, HttpStatus.UNAUTHORIZED, ApiMessages.UNAUTHORIZED))
						.accessDeniedHandler((request, response, e) ->
								writer.writeError(response, HttpStatus.FORBIDDEN, ApiMessages.FORBIDDEN)))
				// ログイン画面・ログアウトは Spring Security が用意するものを使わない（A-01・A-02 で作った。auth 機能）
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.logout(logout -> logout.disable())
				.requestCache(cache -> cache.disable());
		return http.build();
	}
}
