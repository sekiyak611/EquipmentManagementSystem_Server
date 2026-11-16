package jp.co.highjunk.equipment.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * 画面を開いたとき、同じ「ログインしたまま」のクッキーで、複数のAPIがほぼ同時に来ても、盗まれたと見ず、
 * 全部が本人として入れること（詳細設計 common.md 3.17 の判断 No.7）のテスト。
 * 同時に動かすため、まとまり（トランザクション）を作らず、本番と同じ動きで確かめる。終わったら、足した行を自分で消す。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ログインしたままにする：同時の自動ログイン")
class RememberMeConcurrencyTest {

	private static final String IPPAN = "ippan@example.com";

	private static final int REQUESTS = 6;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@BeforeEach
	@AfterEach
	void clean() {
		jdbc.update("DELETE FROM persistent_logins WHERE username = ?", IPPAN);
	}

	@Test
	@DisplayName("同じクッキーで6件が同時に来ても、全部 200。盗まれたと見て行を消すことはなく、行は1つのまま")
	void allRequestsSucceed() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/auth/login")
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + IPPAN + "\",\"password\":\"devpass-1234\",\"rememberMe\":true}"))
				.andReturn();
		Cookie cookie = login.getResponse().getCookie("remember-me");
		assertThat(cookie).isNotNull();

		ExecutorService pool = Executors.newFixedThreadPool(REQUESTS);
		CountDownLatch ready = new CountDownLatch(REQUESTS);
		CountDownLatch go = new CountDownLatch(1);
		List<Future<Integer>> results = new ArrayList<>();
		try {
			for (int i = 0; i < REQUESTS; i++) {
				results.add(pool.submit(() -> {
					ready.countDown();
					go.await();
					return mockMvc.perform(get("/api/auth/me").cookie(cookie)).andReturn().getResponse().getStatus();
				}));
			}
			assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
			go.countDown();
			for (Future<Integer> result : results) {
				assertThat(result.get(30, TimeUnit.SECONDS)).isEqualTo(200);
			}
		} finally {
			pool.shutdownNow();
		}

		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM persistent_logins WHERE username = ?", Integer.class,
				IPPAN)).isEqualTo(1);
	}
}
