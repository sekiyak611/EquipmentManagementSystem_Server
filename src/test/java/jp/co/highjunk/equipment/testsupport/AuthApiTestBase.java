package jp.co.highjunk.equipment.testsupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;


/**
 * api.auth の API と、ログインの部品（common.security）のテストで共通に使う値と道具（API ごとにパッケージが分かれるので testsupport に置く。CLAUDE.md 5章）。もとは auth 機能の API のテストで共通に使う値と道具（LoginApiTest・LogoutApiTest・MeApiTest・LoginUserRefresherApiTest）。
 * ユーザは、開発用のユーザ（common.md 4章。パスワード devpass-1234）を使う。
 * 書き込むテストは、終わるたびに元に戻す（@Transactional。docs/テスト方針.md 4章）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AuthApiTestBase {

	public static final String SOUMU = "soumu@example.com";

	public static final String IPPAN = "ippan@example.com";

	public static final String PASSWORD = "devpass-1234";

	public static final String LOGIN_FAILED = "メールアドレスまたはパスワードが違います";

	public static final String NOW = "'2026-11-08 10:00:00'";

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected JdbcTemplate jdbc;

	@Autowired
	protected SqlSessionTemplate sqlSession;

	/**
	 * テストのデータベースの中身を書き換える。
	 * テストは1つのまとまり（トランザクション）の中で動くので、MyBatis が前に読んだ結果を覚えている（一次キャッシュ）。
	 * JdbcTemplate で書き換えたあとは、それを捨ててから次の API を呼ぶ（本番では API ごとに捨てられるので起きない）。
	 */
	protected void change(String sql, Object... args) {
		jdbc.update(sql, args);
		sqlSession.clearCache();
	}





	protected ResultActions login(String email, String password) throws Exception {
		return postLogin(loginJson(email, password));
	}

	protected ResultActions postLogin(String json) throws Exception {
		return mockMvc.perform(post("/api/auth/login")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	protected MockHttpSession loginSession(String email) throws Exception {
		MvcResult result = login(email, PASSWORD).andExpect(status().isOk()).andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	protected ResultActions me(MockHttpSession session) throws Exception {
		return mockMvc.perform(get("/api/auth/me").session(session));
	}

	protected static String loginJson(String email, String password) {
		return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
	}

	/** 指定した長さの、形の正しいメールアドレス（ローカル部は64文字まで、ドメインの各部分は63文字まで）。 */
	protected static String emailOfLength(int length) {
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

	protected long userId(String email) {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
	}

	protected long roleId(String email) {
		return jdbc.queryForObject("SELECT role_id FROM users WHERE email = ?", Long.class, email);
	}
}
