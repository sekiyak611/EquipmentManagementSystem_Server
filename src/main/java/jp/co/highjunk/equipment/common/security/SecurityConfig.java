package jp.co.highjunk.equipment.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;

import jp.co.highjunk.equipment.common.error.ApiMessages;
import jp.co.highjunk.equipment.common.error.ApiResponseWriter;

/**
 * ログインと権限の確かめ方の土台（docs/API設計.md 2.2、docs/基本設計書.md 5章）。
 * ログインそのもの（A-01）は ZPOP-25 で作る。
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

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiResponseWriter writer) throws Exception {
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
				.csrf(csrf -> csrf.spa())
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
				// ログイン画面・ログアウトは Spring Security が用意するものを使わない（A-01・A-02 で作る）
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.logout(logout -> logout.disable())
				.requestCache(cache -> cache.disable());
		return http.build();
	}
}
