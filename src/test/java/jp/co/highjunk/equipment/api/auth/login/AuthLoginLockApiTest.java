package jp.co.highjunk.equipment.api.auth.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkTokens;
import jp.co.highjunk.equipment.common.slack.FakeSlackSender;
import jp.co.highjunk.equipment.common.slack.SentSlackMessage;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;

/**
 * A-01 ログインの、パスワードを間違えた回数を数える・ロックする（詳細設計 auth/A-01_ログイン.md 4.1。ZPOP-124）のテスト。
 */
@DisplayName("A-01 ログイン：失敗の回数とロック")
class AuthLoginLockApiTest extends AuthApiTestBase {

	private static final String WRONG = "wrong-password";

	@Autowired
	FakeSlackSender slack;

	@BeforeEach
	void clearSlack() {
		slack.clear();
	}

	private void wrong(String email, int times) throws Exception {
		for (int i = 0; i < times; i++) {
			login(email, WRONG).andExpect(status().isUnauthorized());
		}
	}

	private int failedCount(String email) {
		return jdbc.queryForObject("SELECT failed_login_count FROM users WHERE email = ?", Integer.class, email);
	}

	private Object lockedAt(String email) {
		return jdbc.queryForObject("SELECT locked_at FROM users WHERE email = ?", Object.class, email);
	}

	private List<Map<String, Object>> tokens(String email) {
		return jdbc.queryForList("SELECT * FROM password_tokens WHERE user_id = ? ORDER BY id", userId(email));
	}

	@Test
	@DisplayName("パスワードを間違えると、回数が1増える。画面の文・401 は変わらず、ロックもDMもない")
	void countsOneFailure() throws Exception {
		login(IPPAN, WRONG)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(LOGIN_FAILED));

		assertThat(failedCount(IPPAN)).isEqualTo(1);
		assertThat(lockedAt(IPPAN)).isNull();
		assertThat(slack.findAll()).isEmpty();
		assertThat(tokens(IPPAN)).isEmpty();
	}

	@Test
	@DisplayName("4回続けて間違えても、まだロックしない（境界値）。DMもない")
	void fourFailuresDoNotLock() throws Exception {
		wrong(IPPAN, 4);

		assertThat(failedCount(IPPAN)).isEqualTo(4);
		assertThat(lockedAt(IPPAN)).isNull();
		assertThat(slack.findAll()).isEmpty();
		login(IPPAN, PASSWORD).andExpect(status().isOk());
	}

	@Test
	@DisplayName("5回目の間違いでロックする。401・同じ文のまま。LOCK のリンクを作り、本人にDMを1通だけ送る")
	void fifthFailureLocks() throws Exception {
		wrong(IPPAN, 4);

		login(IPPAN, WRONG)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(LOGIN_FAILED));

		assertThat(failedCount(IPPAN)).isEqualTo(5);
		assertThat(lockedAt(IPPAN)).isNotNull();

		List<Map<String, Object>> tokens = tokens(IPPAN);
		assertThat(tokens).hasSize(1);
		assertThat(tokens.get(0).get("purpose")).isEqualTo("LOCK");
		assertThat(tokens.get(0).get("used_at")).isNull();
		assertThat(tokens.get(0).get("revoked_at")).isNull();

		List<SentSlackMessage> messages = slack.findAll();
		assertThat(messages).hasSize(1);
		SentSlackMessage dm = messages.get(0);
		assertThat(dm.getEmail()).isEqualTo(IPPAN);
		assertThat(dm.getTitle()).isEqualTo("【備品管理】アカウントをロックしました");
		assertThat(dm.getBody()).contains("5回").contains("30分").doesNotContain(WRONG).doesNotContain(PASSWORD);

		// DM のリンクの合言葉のハッシュが、保存したハッシュと同じ
		String url = dm.getBody().lines().filter(l -> l.contains("/password-set?token=")).findFirst().orElseThrow();
		String token = url.substring(url.indexOf("token=") + "token=".length()).trim();
		assertThat(PasswordLinkTokens.hash(token)).isEqualTo(tokens.get(0).get("token_hash"));
	}

	@Test
	@DisplayName("ロックされたあとは、正しいパスワードでも 401。間違いを続けても、回数は増えず、DMも送らない")
	void lockedUserIsNotCountedAgain() throws Exception {
		wrong(IPPAN, 5);
		slack.clear();

		wrong(IPPAN, 3);
		login(IPPAN, PASSWORD)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(LOGIN_FAILED));

		assertThat(failedCount(IPPAN)).isEqualTo(5);
		assertThat(slack.findAll()).isEmpty();
		assertThat(tokens(IPPAN)).hasSize(1);
	}

	@Test
	@DisplayName("ロックしたとき、前のリンクがあれば無効になる")
	void lockRevokesOldLinks() throws Exception {
		change("INSERT INTO password_tokens (user_id, token_hash, purpose, expires_at, created_at) VALUES (?, ?, 'RESET', '2999-01-01 00:00:00', "
				+ NOW + ")", userId(IPPAN), "a".repeat(64));

		wrong(IPPAN, 5);

		List<Map<String, Object>> tokens = tokens(IPPAN);
		assertThat(tokens).hasSize(2);
		assertThat(tokens.get(0).get("revoked_at")).isNotNull();
		assertThat(tokens.get(1).get("revoked_at")).isNull();
	}

	@Test
	@DisplayName("ログインに成功すると、失敗の回数が0に戻る。そのあと4回間違えても、ロックしない")
	void successResetsCount() throws Exception {
		wrong(IPPAN, 3);
		assertThat(failedCount(IPPAN)).isEqualTo(3);

		login(IPPAN, PASSWORD).andExpect(status().isOk());
		assertThat(failedCount(IPPAN)).isZero();

		wrong(IPPAN, 4);
		assertThat(lockedAt(IPPAN)).isNull();
		assertThat(slack.findAll()).isEmpty();
	}

	@Test
	@DisplayName("回数が0のときにログインに成功しても、ユーザの行は書き換えない（updated_at が変わらない）")
	void successWithZeroDoesNotWrite() throws Exception {
		change("UPDATE users SET updated_at = " + NOW + " WHERE email = ?", IPPAN);

		login(IPPAN, PASSWORD).andExpect(status().isOk());

		Integer same = jdbc.queryForObject("SELECT updated_at = " + NOW + " FROM users WHERE email = ?",
				Integer.class, IPPAN);
		assertThat(same).isEqualTo(1);
	}

	@Test
	@DisplayName("失敗を数えたとき、updated_by と version は変えない")
	void keepsUpdatedByAndVersion() throws Exception {
		Map<String, Object> before = jdbc.queryForMap("SELECT updated_by, version FROM users WHERE email = ?", IPPAN);

		wrong(IPPAN, 5);

		Map<String, Object> after = jdbc.queryForMap("SELECT updated_by, version FROM users WHERE email = ?", IPPAN);
		assertThat(after).isEqualTo(before);
	}

	@Test
	@DisplayName("登録のないメールアドレスで何度間違えても、だれの回数も増えない。DMもない")
	void unknownEmailIsNotCounted() throws Exception {
		wrong("nobody@example.com", 6);

		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE failed_login_count > 0", Integer.class))
				.isZero();
		assertThat(slack.findAll()).isEmpty();
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM password_tokens", Integer.class)).isZero();
	}

	@Test
	@DisplayName("パスワードがまだない人（最初の総務の担当者）は、数えない")
	void noPasswordIsNotCounted() throws Exception {
		wrong("admin@example.com", 6);

		assertThat(failedCount("admin@example.com")).isZero();
		assertThat(lockedAt("admin@example.com")).isNull();
		assertThat(slack.findAll()).isEmpty();
	}

	@Test
	@DisplayName("削除されたユーザは、数えない")
	void deletedUserIsNotCounted() throws Exception {
		change("UPDATE users SET deleted_at = " + NOW + " WHERE email = ?", IPPAN);

		wrong(IPPAN, 6);

		assertThat(failedCount(IPPAN)).isZero();
		assertThat(lockedAt(IPPAN)).isNull();
		assertThat(slack.findAll()).isEmpty();
	}

	@Test
	@DisplayName("メールアドレスの大文字・小文字がちがっても、同じユーザの失敗として数える")
	void countsIgnoringEmailCase() throws Exception {
		wrong("Ippan@Example.COM", 5);

		assertThat(failedCount(IPPAN)).isEqualTo(5);
		assertThat(lockedAt(IPPAN)).isNotNull();
		assertThat(slack.findAll()).hasSize(1);
		assertThat(slack.findAll().get(0).getEmail()).isEqualTo(IPPAN);
	}

	@Test
	@DisplayName("ほかのユーザの回数には影響しない")
	void doesNotAffectOthers() throws Exception {
		wrong(IPPAN, 5);

		assertThat(failedCount(SOUMU)).isZero();
		assertThat(lockedAt(SOUMU)).isNull();
		login(SOUMU, PASSWORD).andExpect(status().isOk());
	}
}
