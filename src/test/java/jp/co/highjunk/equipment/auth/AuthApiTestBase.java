package jp.co.highjunk.equipment.auth;

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

import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * auth 機能の API のテストで共通に使う値と道具（LoginApiTest・LogoutApiTest・MeApiTest・LoginUserRefresherApiTest）。
 * ユーザは、開発用のユーザ（common.md 4章。パスワード devpass-1234）を使う。
 * 書き込むテストは、終わるたびに元に戻す（@Transactional。docs/テスト方針.md 4章）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class AuthApiTestBase {

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





	ResultActions login(String email, String password) throws Exception {
		return postLogin(loginJson(email, password));
	}

	ResultActions postLogin(String json) throws Exception {
		return mockMvc.perform(post("/api/auth/login")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	MockHttpSession loginSession(String email) throws Exception {
		MvcResult result = login(email, PASSWORD).andExpect(status().isOk()).andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	ResultActions me(MockHttpSession session) throws Exception {
		return mockMvc.perform(get("/api/auth/me").session(session));
	}

	static String loginJson(String email, String password) {
		return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
	}

	/** 指定した長さの、形の正しいメールアドレス（ローカル部は64文字まで、ドメインの各部分は63文字まで）。 */
	static String emailOfLength(int length) {
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

	long userId(String email) {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
	}

	long roleId(String email) {
		return jdbc.queryForObject("SELECT role_id FROM users WHERE email = ?", Long.class, email);
	}
}
