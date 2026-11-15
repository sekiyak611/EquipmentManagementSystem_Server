package jp.co.highjunk.equipment.api.auth.passwordresetrequests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * 同じ人への依頼が同時に来たとき、リンクとDMが1つだけになること（詳細設計 auth/A-04_パスワード設定リンクの依頼.md の8章・12章 No.4）のテスト。
 * 同時に動かすため、まとまり（トランザクション）を作らず、本番と同じ動きで確かめる。終わったら、足したリンクを自分で消す。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("A-04 パスワード設定用のリンクの依頼：同時に依頼されたとき")
class AuthPasswordResetRequestsConcurrencyTest {

	private static final String IPPAN = "ippan@example.com";

	private static final int REQUESTS = 6;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	FakeSlackSender slack;

	@BeforeEach
	void clean() {
		cleanUp();
	}

	@AfterEach
	void cleanUp() {
		jdbc.update("DELETE FROM password_tokens WHERE user_id = (SELECT id FROM users WHERE email = ?)", IPPAN);
		slack.clear();
	}

	@Test
	@DisplayName("同じ人への依頼が6件同時に来ても、リンクは1つ、DMも1通。どれも 202")
	void onlyOneLinkAndOneDm() throws Exception {
		// CSRF の合言葉は、動かす前に用意する（スレッドごとに作ると、それも競争になるため）
		List<RequestPostProcessor> tokens = new ArrayList<>();
		for (int i = 0; i < REQUESTS; i++) {
			tokens.add(CsrfTestHelper.csrfToken(mockMvc));
		}

		ExecutorService pool = Executors.newFixedThreadPool(REQUESTS);
		CountDownLatch ready = new CountDownLatch(REQUESTS);
		CountDownLatch go = new CountDownLatch(1);
		List<Future<Integer>> results = new ArrayList<>();
		try {
			for (int i = 0; i < REQUESTS; i++) {
				RequestPostProcessor csrf = tokens.get(i);
				results.add(pool.submit(() -> {
					ready.countDown();
					go.await();
					return mockMvc.perform(post("/api/auth/password-reset-requests")
							.with(csrf)
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"email\":\"" + IPPAN + "\"}"))
							.andReturn().getResponse().getStatus();
				}));
			}
			assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
			go.countDown();
			for (Future<Integer> result : results) {
				assertThat(result.get(30, TimeUnit.SECONDS)).isEqualTo(202);
			}
		} finally {
			pool.shutdownNow();
		}

		assertThat(jdbc.queryForObject(
				"SELECT COUNT(*) FROM password_tokens WHERE user_id = (SELECT id FROM users WHERE email = ?) AND purpose = 'RESET'",
				Integer.class, IPPAN)).isEqualTo(1);
		assertThat(jdbc.queryForObject(
				"SELECT COUNT(*) FROM password_tokens WHERE user_id = (SELECT id FROM users WHERE email = ?) AND revoked_at IS NOT NULL",
				Integer.class, IPPAN)).isZero();
		assertThat(slack.findAll()).hasSize(1);
	}
}
