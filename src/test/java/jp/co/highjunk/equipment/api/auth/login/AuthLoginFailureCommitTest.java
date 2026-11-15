package jp.co.highjunk.equipment.api.auth.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * 失敗の回数とロックが、401 を返したあとも「確定して」残ること（詳細設計 auth/A-01_ログイン.md 4.1・8章）のテスト。
 * ほかのテストは、テストのあとに元に戻すため全体を1つのまとまり（トランザクション）にしているが、それだと、
 * ServiceImpl のロールバックで回数が消えてしまう不具合に気づけない。ここはまとまりを作らず、本番と同じ動きで確かめ、
 * 終わったら、書き換えた行を自分で元に戻す。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("A-01 ログイン：失敗の回数・ロックが確定して残る")
class AuthLoginFailureCommitTest {

	private static final String IPPAN = "ippan@example.com";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	FakeSlackSender slack;

	private Map<String, Object> original;

	@BeforeEach
	void remember() {
		original = jdbc.queryForMap("SELECT failed_login_count, locked_at, updated_at FROM users WHERE email = ?",
				IPPAN);
		slack.clear();
	}

	@AfterEach
	void restore() {
		jdbc.update("DELETE FROM password_tokens WHERE user_id = (SELECT id FROM users WHERE email = ?)", IPPAN);
		jdbc.update("UPDATE users SET failed_login_count = ?, locked_at = ?, updated_at = ? WHERE email = ?",
				original.get("failed_login_count"), original.get("locked_at"), original.get("updated_at"), IPPAN);
		slack.clear();
	}

	private void wrongLogin() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + IPPAN + "\",\"password\":\"wrong-password\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("401 を返したあとも、失敗の回数は残る。5回目でロックも、リンクも、DMも確定して残る")
	void failuresAndLockAreCommitted() throws Exception {
		wrongLogin();
		assertThat(jdbc.queryForObject("SELECT failed_login_count FROM users WHERE email = ?", Integer.class, IPPAN))
				.isEqualTo(1);

		for (int i = 0; i < 4; i++) {
			wrongLogin();
		}
		assertThat(jdbc.queryForObject("SELECT failed_login_count FROM users WHERE email = ?", Integer.class, IPPAN))
				.isEqualTo(5);
		assertThat(jdbc.queryForObject("SELECT locked_at FROM users WHERE email = ?", Object.class, IPPAN)).isNotNull();
		assertThat(jdbc.queryForObject(
				"SELECT COUNT(*) FROM password_tokens WHERE user_id = (SELECT id FROM users WHERE email = ?) AND purpose = 'LOCK'",
				Integer.class, IPPAN)).isEqualTo(1);
		assertThat(slack.findAll()).hasSize(1);
	}
}
