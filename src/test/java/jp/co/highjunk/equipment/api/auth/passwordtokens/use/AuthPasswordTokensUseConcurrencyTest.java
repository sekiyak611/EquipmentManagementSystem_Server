package jp.co.highjunk.equipment.api.auth.passwordtokens.use;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkIssuer;
import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * 同じリンクが同時に2回以上使われたとき、パスワードが変わるのは1回だけであること（詳細設計 auth/A-06_合言葉でパスワードを決める.md の
 * 5章 No.10・8章）のテスト。同時に動かすため、まとまり（トランザクション）を作らず、本番と同じ動きで確かめる。
 * 終わったら、書き換えたユーザの行とリンクを自分で元に戻す。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("A-06 合言葉でパスワードを決める：同じリンクが同時に使われたとき")
class AuthPasswordTokensUseConcurrencyTest {

	private static final String IPPAN = "ippan@example.com";

	private static final String PREFIX = "http://localhost:8080/password-set?token=";

	private static final int REQUESTS = 4;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	FakeSlackSender slack;

	@Autowired
	PasswordLinkIssuer issuer;

	@Autowired
	PasswordEncoder passwordEncoder;

	private Map<String, Object> original;

	private long userId;

	@BeforeEach
	void remember() {
		original = jdbc.queryForMap(
				"SELECT id, password_hash, failed_login_count, locked_at, session_valid_after, updated_at, updated_by FROM users WHERE email = ?",
				IPPAN);
		userId = ((Number) original.get("id")).longValue();
		jdbc.update("DELETE FROM password_tokens WHERE user_id = ?", userId);
		slack.clear();
	}

	@AfterEach
	void restore() {
		jdbc.update("DELETE FROM password_tokens WHERE user_id = ?", userId);
		jdbc.update(
				"UPDATE users SET password_hash = ?, failed_login_count = ?, locked_at = ?, session_valid_after = ?, updated_at = ?, updated_by = ? WHERE id = ?",
				original.get("password_hash"), original.get("failed_login_count"), original.get("locked_at"),
				original.get("session_valid_after"), original.get("updated_at"), original.get("updated_by"), userId);
		slack.clear();
	}

	@Test
	@DisplayName("同じリンクで、別々のパスワードを4件同時に送っても、成功は1件だけ。あとは 400。反映されるのは成功した1つのパスワードだけ。DMも1通")
	void onlyOneSucceeds() throws Exception {
		String token = issuer.issue(userId, PasswordLinkPurpose.RESET).getLinkUrl().substring(PREFIX.length());
		// CSRF の合言葉は、動かす前に用意する（スレッドごとに作ると、それも競争になるため）
		List<RequestPostProcessor> csrfTokens = new ArrayList<>();
		for (int i = 0; i < REQUESTS; i++) {
			csrfTokens.add(CsrfTestHelper.csrfToken(mockMvc));
		}

		ExecutorService pool = Executors.newFixedThreadPool(REQUESTS);
		CountDownLatch ready = new CountDownLatch(REQUESTS);
		CountDownLatch go = new CountDownLatch(1);
		List<Future<Integer>> results = new ArrayList<>();
		try {
			for (int i = 0; i < REQUESTS; i++) {
				RequestPostProcessor csrf = csrfTokens.get(i);
				String password = "Concurrent-Pass-" + i;
				results.add(pool.submit(() -> {
					ready.countDown();
					go.await();
					return mockMvc.perform(post("/api/auth/password-tokens/use")
							.with(csrf)
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"token\":\"" + token + "\",\"password\":\"" + password
									+ "\",\"passwordConfirm\":\"" + password + "\"}"))
							.andReturn().getResponse().getStatus();
				}));
			}
			assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
			go.countDown();
			List<Integer> statuses = new ArrayList<>();
			for (Future<Integer> result : results) {
				statuses.add(result.get(30, TimeUnit.SECONDS));
			}
			assertThat(statuses.stream().filter(s -> s == 200).count()).isEqualTo(1);
			assertThat(statuses.stream().filter(s -> s == 400).count()).isEqualTo(REQUESTS - 1);
		} finally {
			pool.shutdownNow();
		}

		String hash = jdbc.queryForObject("SELECT password_hash FROM users WHERE id = ?", String.class, userId);
		long matching = 0;
		for (int i = 0; i < REQUESTS; i++) {
			if (passwordEncoder.matches("Concurrent-Pass-" + i, hash)) {
				matching++;
			}
		}
		assertThat(matching).as("反映されたパスワードは、成功した1つだけ").isEqualTo(1);
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM password_tokens WHERE user_id = ? AND used_at IS NOT NULL",
				Integer.class, userId)).isEqualTo(1);
		assertThat(slack.findAll()).hasSize(1);
	}
}
