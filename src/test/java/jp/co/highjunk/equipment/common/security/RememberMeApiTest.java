package jp.co.highjunk.equipment.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import jakarta.servlet.http.Cookie;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkIssuer;
import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * 「ログインしたままにする」（60日間。詳細設計 common.md 3.17。ZPOP-125）のテスト。
 */
@DisplayName("ログインしたままにする")
class RememberMeApiTest extends AuthApiTestBase {

	private static final String COOKIE = "remember-me";

	private static final long SIXTY_DAYS_SECONDS = 60L * 24 * 60 * 60;

	@Autowired
	Clock clock;

	@Autowired
	PasswordLinkIssuer issuer;

	@Autowired
	LoginRememberMe loginRememberMe;

	/** 「ログインしたままにする」にチェックを付けてログインする。 */
	private MvcResult loginRemembered(String email) throws Exception {
		return mockMvc.perform(post("/api/auth/login")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\",\"rememberMe\":true}"))
				.andExpect(status().isOk())
				.andReturn();
	}

	/**
	 * 応答で渡された remember-me のクッキー。同じ名前が2つあるときは、あとのものを使う（ブラウザも、あとのものにする。
	 * 例：自動ログインで合言葉を替えたあと、同じ操作の中で、消す）。なければ null。
	 */
	private static Cookie rememberCookie(MvcResult result) {
		Cookie found = null;
		for (Cookie cookie : result.getResponse().getCookies()) {
			if (COOKIE.equals(cookie.getName())) {
				found = cookie;
			}
		}
		return found;
	}

	/** クッキーの値（番号:合言葉 を Base64 にしたもの）から、番号と合言葉を取り出す。 */
	private static String[] decode(Cookie cookie) {
		// クッキーの値は URL の形（= が %3D）になっている
		String value = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
		while (value.length() % 4 != 0) {
			value += "=";
		}
		// 番号と合言葉も、それぞれ URL の形になっている
		String[] parts = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8).split(":");
		for (int i = 0; i < parts.length; i++) {
			parts[i] = URLDecoder.decode(parts[i], StandardCharsets.UTF_8);
		}
		return parts;
	}

	private ResultActions meWithCookie(Cookie cookie) throws Exception {
		return mockMvc.perform(get("/api/auth/me").cookie(cookie));
	}

	private List<Map<String, Object>> rows(String email) {
		return jdbc.queryForList("SELECT * FROM persistent_logins WHERE username = ? ORDER BY series", email);
	}

	@Test
	@DisplayName("チェックを付けてログイン：クッキー remember-me（HttpOnly・60日間）を渡し、persistent_logins に、その人のメールアドレスの行ができる")
	void loginWithRememberMe() throws Exception {
		Cookie cookie = rememberCookie(loginRemembered(IPPAN));

		assertThat(cookie).isNotNull();
		assertThat(cookie.isHttpOnly()).isTrue();
		assertThat(cookie.getMaxAge()).isEqualTo((int) SIXTY_DAYS_SECONDS);
		assertThat(cookie.getPath()).isEqualTo("/");
		List<Map<String, Object>> rows = rows(IPPAN);
		assertThat(rows).hasSize(1);
		assertThat(decode(cookie)[0]).isEqualTo(rows.get(0).get("series"));
		assertThat(decode(cookie)[1]).isEqualTo(rows.get(0).get("token"));
	}

	@Test
	@DisplayName("チェックを付けない・rememberMe がないログイン：クッキーも行もできない")
	void loginWithoutRememberMe() throws Exception {
		MvcResult result = login(IPPAN, PASSWORD).andExpect(status().isOk()).andReturn();
		assertThat(rememberCookie(result)).isNull();

		MvcResult explicitFalse = mockMvc.perform(post("/api/auth/login")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + IPPAN + "\",\"password\":\"" + PASSWORD + "\",\"rememberMe\":false}"))
				.andExpect(status().isOk()).andReturn();
		assertThat(rememberCookie(explicitFalse)).isNull();
		assertThat(rows(IPPAN)).isEmpty();
	}

	@Test
	@DisplayName("セッションがなくても、クッキーだけで自動ログインできる。合言葉は新しいものに替わり（番号は同じ）、新しいクッキーが渡される")
	void autoLoginRotatesToken() throws Exception {
		Cookie first = rememberCookie(loginRemembered(IPPAN));

		MvcResult result = meWithCookie(first)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(IPPAN))
				.andReturn();

		Cookie second = rememberCookie(result);
		assertThat(second).isNotNull();
		assertThat(decode(second)[0]).isEqualTo(decode(first)[0]);
		assertThat(decode(second)[1]).isNotEqualTo(decode(first)[1]);
		List<Map<String, Object>> rows = rows(IPPAN);
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).get("token")).isEqualTo(decode(second)[1]);
	}

	@Test
	@DisplayName("自動ログインしたあとは、セッションで続く（クッキーなしでも、同じセッションなら使える。合言葉は、そのたびには替わらない）")
	void sessionContinuesAfterAutoLogin() throws Exception {
		Cookie first = rememberCookie(loginRemembered(IPPAN));
		MvcResult result = meWithCookie(first).andExpect(status().isOk()).andReturn();
		MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
		assertThat(session).isNotNull();
		String token = (String) rows(IPPAN).get(0).get("token");

		me(session).andExpect(status().isOk());

		assertThat(rows(IPPAN).get(0).get("token")).isEqualTo(token);
	}

	@Test
	@DisplayName("自動ログインできた人の権限項目は、ログインのときと同じ（読み直して返す）")
	void autoLoginPermissions() throws Exception {
		MvcResult login = loginRemembered(SOUMU);
		Cookie cookie = rememberCookie(login);

		meWithCookie(cookie)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.roleName").value("総務"))
				.andExpect(jsonPath("$.data.permissions.length()").value(18));
	}

	@Test
	@DisplayName("合言葉を替えた直後（10秒以内）に、替える前のクッキーでもう一度来ても（画面を開いたときの、ほぼ同時の呼び出し）、盗まれたとは見ない：200。合言葉は替えず、新しいクッキーも渡さない")
	void oldCookieRightAfterRotationIsAccepted() throws Exception {
		Cookie first = rememberCookie(loginRemembered(IPPAN));
		MvcResult rotated = meWithCookie(first).andExpect(status().isOk()).andReturn();
		Cookie second = rememberCookie(rotated);

		MvcResult again = meWithCookie(first)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(IPPAN))
				.andReturn();

		assertThat(rememberCookie(again)).isNull();
		List<Map<String, Object>> rows = rows(IPPAN);
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).get("token")).isEqualTo(decode(second)[1]);
		meWithCookie(second).andExpect(status().isOk());
	}

	@Test
	@DisplayName("猶予（10秒）をすぎたあとに、替える前のクッキーをもう一度使うと（盗まれたと見る）：401。その人の行がすべて消える（ほかの端末の分も）")
	void oldCookieMeansTheft() throws Exception {
		Cookie first = rememberCookie(loginRemembered(IPPAN));
		rememberCookie(loginRemembered(IPPAN));
		assertThat(rows(IPPAN)).hasSize(2);
		meWithCookie(first).andExpect(status().isOk());
		// 猶予をすぎた状態にする
		loginRememberMe.clearRecentRotations();

		meWithCookie(first)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("ログインしてください。"));

		assertThat(rows(IPPAN)).isEmpty();
	}

	@Test
	@DisplayName("60日をすぎたクッキー（last_used が60日より前）：401。クッキーは消される")
	void expired() throws Exception {
		Cookie cookie = rememberCookie(loginRemembered(IPPAN));
		change("UPDATE persistent_logins SET last_used = ? WHERE username = ?", LocalDateTime.now(clock).minusDays(61),
				IPPAN);

		MvcResult result = meWithCookie(cookie).andExpect(status().isUnauthorized()).andReturn();

		assertThat(rememberCookie(result)).isNotNull();
		assertThat(rememberCookie(result).getMaxAge()).isZero();
	}

	@Test
	@DisplayName("59日前のクッキーは、まだ使える（境界値）")
	void notYetExpired() throws Exception {
		Cookie cookie = rememberCookie(loginRemembered(IPPAN));
		change("UPDATE persistent_logins SET last_used = ? WHERE username = ?", LocalDateTime.now(clock).minusDays(59),
				IPPAN);

		meWithCookie(cookie).andExpect(status().isOk());
	}

	@Test
	@DisplayName("行がない（消された）番号のクッキー：401。クッキーは消される")
	void unknownSeries() throws Exception {
		Cookie cookie = rememberCookie(loginRemembered(IPPAN));
		change("DELETE FROM persistent_logins WHERE username = ?", IPPAN);

		MvcResult result = meWithCookie(cookie).andExpect(status().isUnauthorized()).andReturn();

		assertThat(rememberCookie(result).getMaxAge()).isZero();
	}

	@Test
	@DisplayName("削除されたユーザ：401。クッキーは消される")
	void deletedUser() throws Exception {
		Cookie cookie = rememberCookie(loginRemembered(IPPAN));
		change("UPDATE users SET deleted_at = " + NOW + " WHERE email = ?", IPPAN);

		MvcResult result = meWithCookie(cookie).andExpect(status().isUnauthorized()).andReturn();

		assertThat(rememberCookie(result).getMaxAge()).isZero();
	}

	@Test
	@DisplayName("読めないクッキー・でたらめなクッキー：500 にならず 401。クッキーは消される")
	void garbageCookie() throws Exception {
		for (String value : new String[] { "not-base64!!", "YWJj", "" }) {
			MvcResult result = meWithCookie(new Cookie(COOKIE, value)).andExpect(status().isUnauthorized()).andReturn();
			if (!value.isEmpty()) {
				assertThat(rememberCookie(result)).isNotNull();
			}
		}
	}

	@Test
	@DisplayName("ロックされている人でも、ログインしたままの端末は、自動ログインできる（画面設計 2.1 の6）")
	void lockedUserCanAutoLogin() throws Exception {
		Cookie cookie = rememberCookie(loginRemembered(IPPAN));
		change("UPDATE users SET locked_at = " + NOW + ", failed_login_count = 5 WHERE email = ?", IPPAN);

		meWithCookie(cookie).andExpect(status().isOk());
	}

	@Test
	@DisplayName("ログアウト（A-02）：この端末の行とクッキーだけが消える。ほかの端末の「ログインしたまま」は残る")
	void logoutRemovesThisDeviceOnly() throws Exception {
		MvcResult thisDevice = loginRemembered(IPPAN);
		Cookie thisCookie = rememberCookie(thisDevice);
		Cookie otherCookie = rememberCookie(loginRemembered(IPPAN));
		MockHttpSession session = (MockHttpSession) thisDevice.getRequest().getSession(false);

		MvcResult result = mockMvc.perform(post("/api/auth/logout")
				.session(session)
				.cookie(thisCookie)
				.with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(rememberCookie(result).getMaxAge()).isZero();
		List<Map<String, Object>> rows = rows(IPPAN);
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).get("series")).isEqualTo(decode(otherCookie)[0]);
		meWithCookie(thisCookie).andExpect(status().isUnauthorized());
		meWithCookie(otherCookie).andExpect(status().isOk());
	}

	@Test
	@DisplayName("ログアウトで、クッキーがない端末：何も消えない（ほかの端末の行は残る）")
	void logoutWithoutCookie() throws Exception {
		rememberCookie(loginRemembered(IPPAN));
		MockHttpSession session = loginSession(IPPAN);

		mockMvc.perform(post("/api/auth/logout").session(session).with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isOk());

		assertThat(rows(IPPAN)).hasSize(1);
	}

	@Test
	@DisplayName("チェックを付けずにログインし直す：この端末の前のクッキーと行が消える。ほかの端末の分は残る")
	void loginWithoutBoxForgetsThisDevice() throws Exception {
		Cookie thisCookie = rememberCookie(loginRemembered(IPPAN));
		Cookie otherCookie = rememberCookie(loginRemembered(IPPAN));

		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.cookie(thisCookie)
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + IPPAN + "\",\"password\":\"" + PASSWORD + "\",\"rememberMe\":false}"))
				.andExpect(status().isOk()).andReturn();

		assertThat(rememberCookie(result).getMaxAge()).isZero();
		assertThat(rows(IPPAN)).hasSize(1);
		assertThat(rows(IPPAN).get(0).get("series")).isEqualTo(decode(otherCookie)[0]);
	}

	@Test
	@DisplayName("別の人のクッキーがある端末で、別の人としてログインすると、ログインした人として入る")
	void loginAsAnotherUser() throws Exception {
		Cookie ippanCookie = rememberCookie(loginRemembered(IPPAN));

		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.cookie(ippanCookie)
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + SOUMU + "\",\"password\":\"" + PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(SOUMU))
				.andReturn();

		me((MockHttpSession) result.getRequest().getSession(false))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(SOUMU));
	}

	@Test
	@DisplayName("パスワードを再設定（A-06）すると、その人の「ログインしたまま」がすべて消える。ほかの人の分は残る")
	void passwordResetRemovesAll() throws Exception {
		Cookie ippan1 = rememberCookie(loginRemembered(IPPAN));
		rememberCookie(loginRemembered(IPPAN));
		Cookie soumu = rememberCookie(loginRemembered(SOUMU));
		String token = issuer.issue(userId(IPPAN), PasswordLinkPurpose.RESET).getLinkUrl()
				.substring("http://localhost:8080/password-set?token=".length());

		mockMvc.perform(post("/api/auth/password-tokens/use")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + token + "\",\"password\":\"Xq7#kd9!Lm2\",\"passwordConfirm\":\"Xq7#kd9!Lm2\"}"))
				.andExpect(status().isOk());

		assertThat(rows(IPPAN)).isEmpty();
		assertThat(rows(SOUMU)).hasSize(1);
		meWithCookie(ippan1).andExpect(status().isUnauthorized());
		meWithCookie(soumu).andExpect(status().isOk());
	}

	private ResultActions changePassword(MockHttpSession session, Cookie cookie, String current, String password)
			throws Exception {
		var request = put("/api/me/password")
				.session(session)
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"currentPassword\":\"" + current + "\",\"password\":\"" + password
						+ "\",\"passwordConfirm\":\"" + password + "\"}");
		if (cookie != null) {
			request.cookie(cookie);
		}
		return mockMvc.perform(request);
	}

	@Test
	@DisplayName("パスワードを変更（A-07）：ほかの端末の「ログインしたまま」は消える。この端末が「ログインしたまま」だったときは、この端末の分だけ新しい番号で作り直す")
	void passwordChangeKeepsThisDevice() throws Exception {
		MvcResult thisDevice = loginRemembered(IPPAN);
		Cookie thisCookie = rememberCookie(thisDevice);
		Cookie otherCookie = rememberCookie(loginRemembered(IPPAN));
		MockHttpSession session = (MockHttpSession) thisDevice.getRequest().getSession(false);

		MvcResult result = changePassword(session, thisCookie, PASSWORD, "Xq7#kd9!Lm2").andExpect(status().isOk())
				.andReturn();

		Cookie newCookie = rememberCookie(result);
		assertThat(newCookie).isNotNull();
		assertThat(decode(newCookie)[0]).isNotEqualTo(decode(thisCookie)[0]).isNotEqualTo(decode(otherCookie)[0]);
		List<Map<String, Object>> rows = rows(IPPAN);
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).get("series")).isEqualTo(decode(newCookie)[0]);
		meWithCookie(otherCookie).andExpect(status().isUnauthorized());
		meWithCookie(newCookie).andExpect(status().isOk());
	}

	@Test
	@DisplayName("パスワードを変更（A-07）：この端末が「ログインしたまま」でなければ、新しく作らない。ほかの端末の分は消える")
	void passwordChangeWithoutCookie() throws Exception {
		rememberCookie(loginRemembered(IPPAN));
		MockHttpSession session = loginSession(IPPAN);

		MvcResult result = changePassword(session, null, PASSWORD, "Xq7#kd9!Lm2").andExpect(status().isOk())
				.andReturn();

		assertThat(rememberCookie(result)).isNull();
		assertThat(rows(IPPAN)).isEmpty();
	}

	@Test
	@DisplayName("パスワードの変更（A-07）に失敗（現在のパスワードが違う）：「ログインしたまま」は、どれも消えない")
	void failedPasswordChangeKeepsAll() throws Exception {
		MvcResult thisDevice = loginRemembered(IPPAN);
		Cookie thisCookie = rememberCookie(thisDevice);
		rememberCookie(loginRemembered(IPPAN));
		MockHttpSession session = (MockHttpSession) thisDevice.getRequest().getSession(false);

		changePassword(session, thisCookie, "wrong-password", "Xq7#kd9!Lm2").andExpect(status().isBadRequest());

		assertThat(rows(IPPAN)).hasSize(2);
	}

	@Test
	@DisplayName("現在のパスワードを5回間違えて、この端末をログアウトさせたとき：この端末の行とクッキーだけが消える。ほかの端末の分は残る")
	void lockLogoutRemovesThisDeviceOnly() throws Exception {
		MvcResult thisDevice = loginRemembered(IPPAN);
		Cookie thisCookie = rememberCookie(thisDevice);
		Cookie otherCookie = rememberCookie(loginRemembered(IPPAN));
		MockHttpSession session = (MockHttpSession) thisDevice.getRequest().getSession(false);

		MvcResult last = null;
		for (int i = 0; i < 5; i++) {
			last = changePassword(session, thisCookie, "wrong-password", "Xq7#kd9!Lm2").andExpect(status().isBadRequest())
					.andReturn();
		}

		assertThat(rememberCookie(last).getMaxAge()).isZero();
		List<Map<String, Object>> rows = rows(IPPAN);
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).get("series")).isEqualTo(decode(otherCookie)[0]);
	}

	@Test
	@DisplayName("画面のファイル（/api/ でないアドレス）では、自動ログインしない（クッキーの合言葉を替えない）")
	void nonApiRequestDoesNotAutoLogin() throws Exception {
		Cookie cookie = rememberCookie(loginRemembered(IPPAN));
		String token = (String) rows(IPPAN).get(0).get("token");

		mockMvc.perform(get("/some-screen").cookie(cookie));

		assertThat(rows(IPPAN).get(0).get("token")).isEqualTo(token);
	}

	@Test
	@DisplayName("ログイン状態もクッキーもなければ、401（自動ログインを試みない）")
	void nothing() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.data").value(nullValue()));
	}
}
