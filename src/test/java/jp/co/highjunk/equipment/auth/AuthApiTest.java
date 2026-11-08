package jp.co.highjunk.equipment.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * A-01 ログイン・A-02 ログアウト・A-03 ログイン中の人の情報（docs/API設計.md 2.2・3.1・4.1・4.2）と、
 * APIを呼ぶたびの権限項目の読み直し（詳細設計 auth/、common.md 3.10）のテスト。
 * ユーザは、開発用のユーザ（common.md 4章。パスワード devpass-1234）を使う。
 * 書き込むテストは、終わるたびに元に戻す（@Transactional。docs/テスト方針.md 4章）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthApiTest {

	static final String SOUMU = "soumu@example.com";

	static final String IPPAN = "ippan@example.com";

	static final String PASSWORD = "devpass-1234";

	static final String LOGIN_FAILED = "メールアドレスまたはパスワードが違います";

	static final String NOW = "'2026-11-08 10:00:00'";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	SqlSessionTemplate sqlSession;

	/**
	 * テストのデータベースの中身を書き換える。
	 * テストは1つのまとまり（トランザクション）の中で動くので、MyBatis が前に読んだ結果を覚えている（一次キャッシュ）。
	 * JdbcTemplate で書き換えたあとは、それを捨ててから次の API を呼ぶ（本番では API ごとに捨てられるので起きない）。
	 */
	void change(String sql, Object... args) {
		jdbc.update(sql, args);
		sqlSession.clearCache();
	}

	@Nested
	@DisplayName("A-01 ログイン")
	class Login {

		@Test
		@DisplayName("総務：200。data に氏名・権限・18個の権限項目（並び順どおり）、最初の画面は備品一覧（EQUIPMENT_LIST）")
		void soumu() throws Exception {
			login(SOUMU, PASSWORD)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value(200))
					.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
					.andExpect(jsonPath("$.data.id").value(userId(SOUMU)))
					.andExpect(jsonPath("$.data.name").value("開発 総務"))
					.andExpect(jsonPath("$.data.email").value(SOUMU))
					.andExpect(jsonPath("$.data.roleName").value("総務"))
					.andExpect(jsonPath("$.data.permissions", hasSize(18)))
					.andExpect(jsonPath("$.data.permissions[0]").value("EQUIPMENT_VIEW"))
					.andExpect(jsonPath("$.data.permissions[17]").value("PERMISSION_MANAGE"))
					.andExpect(jsonPath("$.data.homePage").value("EQUIPMENT_LIST"))
					.andExpect(jsonPath("$.errors").value(nullValue()));
		}

		@Test
		@DisplayName("一般社員：権限項目は LOAN_SELF・CONFIRM_SELF の順、最初の画面は自分の備品（MY_EQUIPMENT）")
		void ippan() throws Exception {
			login(IPPAN, PASSWORD)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.roleName").value("一般社員"))
					.andExpect(jsonPath("$.data.permissions", contains("LOAN_SELF", "CONFIRM_SELF")))
					.andExpect(jsonPath("$.data.homePage").value("MY_EQUIPMENT"));
		}

		@Test
		@DisplayName("メールアドレスの大文字・小文字は区別しない")
		void emailIgnoresCase() throws Exception {
			login("Soumu@Example.COM", PASSWORD)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.email").value(SOUMU));
		}

		@Test
		@DisplayName("ログインすると、ログイン状態がセッションに入り、セッションの番号が変わり、XSRF-TOKEN が作り直される")
		void sessionAndCsrfRenewed() throws Exception {
			MockHttpSession before = new MockHttpSession();
			String beforeId = before.getId();
			Cookie oldToken = mockMvc.perform(get("/api/health")).andReturn().getResponse().getCookie("XSRF-TOKEN");

			MvcResult result = mockMvc.perform(post("/api/auth/login").session(before)
					// ブラウザがセッションのクッキー（JSESSIONID）を送ってきた形にする
					.with(request -> {
						request.setRequestedSessionId(beforeId);
						request.setRequestedSessionIdValid(true);
						return request;
					})
					.with(CsrfTestHelper.csrfToken(mockMvc))
					.contentType(MediaType.APPLICATION_JSON)
					.content(loginJson(SOUMU, PASSWORD)))
					.andExpect(status().isOk())
					.andReturn();

			MockHttpSession after = (MockHttpSession) result.getRequest().getSession(false);
			assertThat(after).isNotNull();
			// MockHttpSession は、番号を変えても同じ入れ物のまま、番号だけが変わる
			assertThat(after.getId()).isNotEqualTo(beforeId);
			Cookie newToken = result.getResponse().getCookie("XSRF-TOKEN");
			assertThat(newToken).isNotNull();
			assertThat(newToken.getValue()).isNotEmpty().isNotEqualTo(oldToken.getValue());
			me(after).andExpect(status().isOk());
		}

		@Test
		@DisplayName("パスワードが違う：401。ログインできない")
		void wrongPassword() throws Exception {
			MvcResult result = login(SOUMU, "wrong-password")
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.status").value(401))
					.andExpect(jsonPath("$.message").value(LOGIN_FAILED))
					.andExpect(jsonPath("$.data").value(nullValue()))
					.andExpect(jsonPath("$.errors").value(nullValue()))
					.andReturn();

			// セッションが作られていない、または作られていてもログインしていない
			MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
			me(session == null ? new MockHttpSession() : session).andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("登録のないメールアドレス：401。パスワードが違うときと同じ文")
		void unknownEmail() throws Exception {
			login("nobody@example.com", PASSWORD)
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.message").value(LOGIN_FAILED));
		}

		@Test
		@DisplayName("削除されたユーザ：正しいパスワードでも 401。同じ文")
		void deletedUser() throws Exception {
			change("UPDATE users SET deleted_at = " + NOW + " WHERE email = ?", IPPAN);

			login(IPPAN, PASSWORD)
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.message").value(LOGIN_FAILED));
		}

		@Test
		@DisplayName("ロック中のユーザ：正しいパスワードでも 401。同じ文（画面設計 2.1）")
		void lockedUser() throws Exception {
			change("UPDATE users SET locked_at = " + NOW + " WHERE email = ?", IPPAN);

			login(IPPAN, PASSWORD)
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.message").value(LOGIN_FAILED));
		}

		@Test
		@DisplayName("パスワードがまだない人（最初の総務の担当者）：401。同じ文")
		void noPasswordYet() throws Exception {
			login("admin@example.com", "")
					.andExpect(status().isBadRequest());
			login("admin@example.com", PASSWORD)
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.message").value(LOGIN_FAILED));
		}

		@Test
		@DisplayName("入力の誤り：メールアドレス・パスワードが空なら 400。入力欄ごとに画面設計の文")
		void blank() throws Exception {
			postLogin("{}")
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
					.andExpect(jsonPath("$.errors[?(@.field == 'email')].message")
							.value("メールアドレスを入力してください"))
					.andExpect(jsonPath("$.errors[?(@.field == 'password')].message")
							.value("パスワードを入力してください"));
		}

		@Test
		@DisplayName("入力の誤り：メールアドレスの形でなければ 400")
		void invalidEmail() throws Exception {
			login("soumu.example.com", PASSWORD)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors[0].field").value("email"))
					.andExpect(jsonPath("$.errors[0].message").value("メールアドレスの形が正しくありません"));
		}

		@Test
		@DisplayName("メールアドレスは254文字まで。254文字は入力の誤りにならず（登録がないので 401）、255文字は 400")
		void emailLength() throws Exception {
			login(emailOfLength(254), PASSWORD)
					.andExpect(status().isUnauthorized());
			login(emailOfLength(255), PASSWORD)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors[*].message").value(
							org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("メールアドレスの形が正しくありません"))));
		}

		@Test
		@DisplayName("ログインしたままにする（rememberMe）を送っても、今はふつうにログインする（ZPOP-125 で作る）")
		void rememberMeIsAccepted() throws Exception {
			postLogin("{\"email\":\"" + SOUMU + "\",\"password\":\"" + PASSWORD + "\",\"rememberMe\":true}")
					.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("A-03 ログイン中の人の情報")
	class Me {

		@Test
		@DisplayName("ログインしていなければ 401（ログインしてください。）")
		void notLoggedIn() throws Exception {
			mockMvc.perform(get("/api/auth/me"))
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.message").value("ログインしてください。"));
		}

		@Test
		@DisplayName("ログインしていれば、A-01 と同じ形で返す")
		void loggedIn() throws Exception {
			me(loginSession(IPPAN))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id").value(userId(IPPAN)))
					.andExpect(jsonPath("$.data.name").value("開発 一般"))
					.andExpect(jsonPath("$.data.email").value(IPPAN))
					.andExpect(jsonPath("$.data.roleName").value("一般社員"))
					.andExpect(jsonPath("$.data.permissions", contains("LOAN_SELF", "CONFIRM_SELF")))
					.andExpect(jsonPath("$.data.homePage").value("MY_EQUIPMENT"))
					.andExpect(jsonPath("$.errors").value(nullValue()));
		}
	}

	@Nested
	@DisplayName("APIを呼ぶたびの読み直し（API設計 2.2）")
	class Refresh {

		@Test
		@DisplayName("権限の変更は、ログインし直さなくても次の操作から効く")
		void permissionChangeTakesEffect() throws Exception {
			MockHttpSession session = loginSession(IPPAN);
			mockMvc.perform(get("/api/test-errors/permission").session(session))
					.andExpect(status().isForbidden());

			change("INSERT INTO role_permissions (role_id, permission_code) VALUES (?, 'EQUIPMENT_UPDATE')",
					roleId(IPPAN));
			change("INSERT INTO role_permissions (role_id, permission_code) VALUES (?, 'EQUIPMENT_VIEW')",
					roleId(IPPAN));

			mockMvc.perform(get("/api/test-errors/permission").session(session))
					.andExpect(status().isOk());
			me(session)
					.andExpect(jsonPath("$.data.permissions",
							contains("EQUIPMENT_VIEW", "EQUIPMENT_UPDATE", "LOAN_SELF", "CONFIRM_SELF")))
					.andExpect(jsonPath("$.data.homePage").value("EQUIPMENT_LIST"));
		}

		@Test
		@DisplayName("ログインしている人が削除されたら、次の操作で 401。セッションも捨てる")
		void deletedAfterLogin() throws Exception {
			MockHttpSession session = loginSession(IPPAN);
			change("UPDATE users SET deleted_at = " + NOW + " WHERE email = ?", IPPAN);

			me(session).andExpect(status().isUnauthorized());
			assertThat(session.isInvalid()).isTrue();
		}

		@Test
		@DisplayName("ログインのあとにパスワードが変わった（session_valid_after がログインより後）なら、次の操作で 401")
		void passwordChangedAfterLogin() throws Exception {
			MockHttpSession session = loginSession(IPPAN);
			change("UPDATE users SET session_valid_after = '2999-01-01 00:00:00' WHERE email = ?", IPPAN);

			me(session).andExpect(status().isUnauthorized());
			assertThat(session.isInvalid()).isTrue();
		}

		@Test
		@DisplayName("session_valid_after がログインより前なら、そのまま使える")
		void passwordChangedBeforeLogin() throws Exception {
			change("UPDATE users SET session_valid_after = '2000-01-01 00:00:00' WHERE email = ?", IPPAN);
			MockHttpSession session = loginSession(IPPAN);

			me(session).andExpect(status().isOk());
		}

		@Test
		@DisplayName("ロックされても、ログインしている人は締め出さない（画面設計 2.1 の6）")
		void lockedAfterLogin() throws Exception {
			MockHttpSession session = loginSession(IPPAN);
			change("UPDATE users SET locked_at = " + NOW + " WHERE email = ?", IPPAN);

			me(session).andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("A-02 ログアウト")
	class Logout {

		@Test
		@DisplayName("ログアウトすると 200（data は null）。そのあとは 401")
		void logout() throws Exception {
			MockHttpSession session = loginSession(SOUMU);
			Cookie oldToken = mockMvc.perform(get("/api/health")).andReturn().getResponse().getCookie("XSRF-TOKEN");

			MvcResult result = mockMvc.perform(post("/api/auth/logout").session(session)
					// ログアウトの前の合言葉（oldToken）で送る
					.cookie(oldToken).header("X-XSRF-TOKEN", oldToken.getValue()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value(200))
					.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
					.andExpect(jsonPath("$.data").value(nullValue()))
					.andExpect(jsonPath("$.errors").value(nullValue()))
					.andReturn();

			assertThat(session.isInvalid()).isTrue();
			Cookie newToken = result.getResponse().getCookie("XSRF-TOKEN");
			assertThat(newToken).isNotNull();
			assertThat(newToken.getValue()).isNotEmpty().isNotEqualTo(oldToken.getValue());
			me(session).andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("ログインしていなければ 401")
		void notLoggedIn() throws Exception {
			mockMvc.perform(post("/api/auth/logout").with(CsrfTestHelper.csrfToken(mockMvc)))
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.message").value("ログインしてください。"));
		}

		@Test
		@DisplayName("CSRF の合言葉がなければ 403。ログアウトしない")
		void withoutCsrf() throws Exception {
			MockHttpSession session = loginSession(SOUMU);

			mockMvc.perform(post("/api/auth/logout").session(session))
					.andExpect(status().isForbidden());
			me(session).andExpect(status().isOk());
		}
	}

	private ResultActions login(String email, String password) throws Exception {
		return postLogin(loginJson(email, password));
	}

	private ResultActions postLogin(String json) throws Exception {
		return mockMvc.perform(post("/api/auth/login")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	private MockHttpSession loginSession(String email) throws Exception {
		MvcResult result = login(email, PASSWORD).andExpect(status().isOk()).andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	private ResultActions me(MockHttpSession session) throws Exception {
		return mockMvc.perform(get("/api/auth/me").session(session));
	}

	private static String loginJson(String email, String password) {
		return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
	}

	/** 指定した長さの、形の正しいメールアドレス（ローカル部は64文字まで、ドメインの各部分は63文字まで）。 */
	private static String emailOfLength(int length) {
		String local = "a".repeat(64);
		StringBuilder domain = new StringBuilder();
		int rest = length - local.length() - 1 - ".com".length();
		while (rest > 0) {
			int part = Math.min(rest, 63);
			if (domain.length() > 0) {
				domain.append('.');
				rest--;
				part = Math.min(rest, 63);
			}
			domain.append("b".repeat(part));
			rest -= part;
		}
		return local + "@" + domain + ".com";
	}

	private long userId(String email) {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
	}

	private long roleId(String email) {
		return jdbc.queryForObject("SELECT role_id FROM users WHERE email = ?", Long.class, email);
	}
}
