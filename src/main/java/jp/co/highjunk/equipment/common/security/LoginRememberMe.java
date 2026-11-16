package jp.co.highjunk.equipment.common.security;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.co.highjunk.equipment.common.config.AppProperties;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;

/**
 * 「ログインしたままにする」（60日間）を、業務から使いやすい形にする部品（詳細設計 common.md 3.17）。
 * Spring Security の PersistentTokenBasedRememberMeServices を使う。クッキー `remember-me` に、端末ごとの番号（series）と
 * 合言葉（token）を持たせ、同じものを persistent_logins にも持つ。自動ログインのたびに、合言葉を新しいものに替える。
 * ログに、クッキーの値（番号・合言葉）を出さない。
 */
@Component
public class LoginRememberMe {

	private static final Logger log = LoggerFactory.getLogger(LoginRememberMe.class);

	/**
	 * 合言葉を新しくしたあと、古い合言葉のクッキーでもう一度来ても、盗まれたとは見ない時間（詳細設計 common.md 3.17 の判断 No.7）。
	 * 画面を開いたとき、ほぼ同時に、複数のAPIが同じクッキーで来る。1つ目で合言葉が替わり、2つ目の古い合言葉を、盗まれたと見てしまうのを防ぐ。
	 */
	private static final Duration GRACE = Duration.ofSeconds(10);

	/** クッキーの名前（Spring Security の初めの名前） */
	static final String COOKIE_NAME = "remember-me";

	private final PerDeviceRememberMeServices services;

	private final RememberMeTokenRepository tokenRepository;

	private final RememberMeUserDetailsService userDetailsService;

	private final UserMapper userMapper;

	/** 番号ごとの、直近で合言葉を替えたときの記録（替える前の合言葉と、替えた時刻）。同じ番号の自動ログインを、1つずつ処理する鍵にも使う。 */
	private final ConcurrentMap<String, Rotation> rotations = new ConcurrentHashMap<>();

	public LoginRememberMe(RememberMeTokenRepository tokenRepository, RememberMeUserDetailsService userDetailsService,
			UserMapper userMapper, AppProperties appProperties,
			@Value("${server.servlet.session.cookie.secure:false}") boolean secureCookie) {
		this.tokenRepository = tokenRepository;
		this.userDetailsService = userDetailsService;
		this.userMapper = userMapper;
		// key は、Spring Security がログイン状態の印に付ける値（ここでは使い道がない。起動のたびに変えてよい）
		this.services = new PerDeviceRememberMeServices(UUID.randomUUID().toString(), userDetailsService,
				tokenRepository);
		this.services.setCookieName(COOKIE_NAME);
		this.services.setTokenValiditySeconds((int) appProperties.rememberMeValidity().toSeconds());
		// remember を呼んだときは、いつも「ログインしたまま」にする（チェックの有無は、呼ぶ側が決める）
		this.services.setAlwaysRemember(true);
		// 本番では、HTTPS のときだけ送る（セッションのクッキーと同じ設定）
		this.services.setUseSecureCookie(secureCookie);
	}

	/** この端末を「ログインしたまま」にする。クッキーを渡し、persistent_logins に1行作る。 */
	public void remember(long userId, HttpServletRequest request, HttpServletResponse response) {
		User user = userMapper.selectByPrimaryKey(userId);
		if (user == null) {
			return;
		}
		org.springframework.security.core.userdetails.User details = new org.springframework.security.core.userdetails.User(
				user.getEmail(), "", List.of());
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(details, null, List.of());
		services.loginSuccess(request, response, authentication);
		log.info("ログインしたままにしました（userId={}）", userId);
	}

	/** この端末のクッキーと、その番号の行だけを消す（ほかの端末の分は残る）。クッキーがなければ、何もしない。 */
	public void forget(HttpServletRequest request, HttpServletResponse response) {
		if (isRemembered(request)) {
			services.logout(request, response, null);
		}
	}

	/** その人の「ログインしたまま」を、すべて消す（パスワードを変えた・再設定したとき）。 */
	public void removeAllOf(String email) {
		tokenRepository.removeUserTokens(email);
	}

	/** この端末に、「ログインしたまま」のクッキーがあるか。 */
	public boolean isRemembered(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return false;
		}
		for (Cookie cookie : cookies) {
			if (COOKIE_NAME.equals(cookie.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * クッキーで、本人と確かめる。確かめられたら、合言葉を新しいものに替えて（クッキーも新しくして）、ユーザ ID を返す。
	 * 合言葉が違う（盗まれたと見る）・60日をすぎた・ユーザがいない・削除された、のときは、空（クッキーは消す。盗まれたと見たときは、その人の行もすべて消す）。
	 * ただし、直前（10秒以内）に合言葉を替えた番号に、替える前の合言葉のクッキーで来たときは、盗まれたとは見ず、本人として入れる
	 * （合言葉は替えず、新しいクッキーも渡さない。先に替えたほうの応答が、新しいクッキーを渡している）。
	 */
	public Optional<Long> autoLogin(HttpServletRequest request, HttpServletResponse response) {
		String[] presented = services.presentedCookie(request);
		if (presented == null) {
			// 読めないクッキー：Spring Security が、クッキーを消す
			return autoLoginOnce(request, response);
		}
		String series = presented[0];
		Rotation rotation = rotations.computeIfAbsent(series, key -> new Rotation());
		// 同じ番号の自動ログインは、1つずつ処理する（ほぼ同時の2つ目は、1つ目が替えたあとに、猶予で入る）
		synchronized (rotation) {
			if (rotation.isWithinGrace(presented[1])) {
				PersistentRememberMeToken current = tokenRepository.getTokenForSeries(series);
				if (current != null) {
					return findUserId(current.getUsername());
				}
			}
			Optional<Long> userId = autoLoginOnce(request, response);
			if (userId.isPresent()) {
				rotation.rotated(presented[1]);
			}
			removeStaleRotations();
			return userId;
		}
	}

	private Optional<Long> autoLoginOnce(HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication;
		try {
			authentication = services.autoLogin(request, response);
		} catch (CookieTheftException e) {
			log.warn("「ログインしたまま」のクッキーが、すでに使われたものでした。盗まれた可能性があるので、その人の「ログインしたまま」をすべて消しました");
			return Optional.empty();
		}
		if (authentication == null) {
			return Optional.empty();
		}
		return findUserId(authentication.getName());
	}

	private Optional<Long> findUserId(String email) {
		User user = userDetailsService.findUser(email);
		return user == null ? Optional.empty() : Optional.of(user.getId());
	}

	/** 猶予をすぎた記録を捨てる（たまり続けないように）。 */
	private void removeStaleRotations() {
		if (rotations.size() > 1000) {
			rotations.entrySet().removeIf(entry -> entry.getValue().isStale());
		}
	}

	/** テスト用：直近の記録を捨てる（猶予をすぎた状態にする）。 */
	void clearRecentRotations() {
		rotations.clear();
	}

	/** 番号ごとの、直近で合言葉を替えたときの記録。 */
	private static class Rotation {

		private String previousToken;

		private long rotatedAtMillis;

		synchronized boolean isWithinGrace(String token) {
			return previousToken != null && previousToken.equals(token)
					&& System.currentTimeMillis() - rotatedAtMillis < GRACE.toMillis();
		}

		synchronized void rotated(String token) {
			this.previousToken = token;
			this.rotatedAtMillis = System.currentTimeMillis();
		}

		synchronized boolean isStale() {
			return System.currentTimeMillis() - rotatedAtMillis >= GRACE.toMillis();
		}
	}

	/**
	 * Spring Security の標準は、ログアウトで、その人のすべての端末の行を消す。
	 * ここでは、この端末の番号の行だけを消す（詳細設計 common.md 3.17 の判断 No.1）。
	 */
	private static class PerDeviceRememberMeServices extends PersistentTokenBasedRememberMeServices {

		private final RememberMeTokenRepository repository;

		PerDeviceRememberMeServices(String key, RememberMeUserDetailsService userDetailsService,
				RememberMeTokenRepository repository) {
			super(key, userDetailsService, repository);
			this.repository = repository;
		}

		/** クッキーの、番号と合言葉。クッキーがない・読めないときは null。 */
		String[] presentedCookie(HttpServletRequest request) {
			String cookie = extractRememberMeCookie(request);
			if (cookie == null || cookie.isEmpty()) {
				return null;
			}
			try {
				String[] tokens = decodeCookie(cookie);
				return tokens.length == 2 ? tokens : null;
			} catch (RuntimeException e) {
				return null;
			}
		}

		@Override
		public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
			String cookie = extractRememberMeCookie(request);
			cancelCookie(request, response);
			if (cookie != null && !cookie.isEmpty()) {
				try {
					String[] tokens = decodeCookie(cookie);
					if (tokens.length == 2) {
						repository.removeSeries(tokens[0]);
					}
				} catch (RuntimeException e) {
					// 読めないクッキー：消すだけ（行は、もとから、この番号ではない）
					log.info("読めない「ログインしたまま」のクッキーを消しました");
				}
			}
		}
	}
}
