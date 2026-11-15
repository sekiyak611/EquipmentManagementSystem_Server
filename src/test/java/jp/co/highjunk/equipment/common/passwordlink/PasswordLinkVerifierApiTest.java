package jp.co.highjunk.equipment.common.passwordlink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;

/**
 * パスワード設定用のリンクの合言葉を確かめる部品（詳細設計 common.md 3.14。ZPOP-59）のテスト。データベースを使う（テストのあとに元に戻す）。
 */
@DisplayName("common.passwordlink 合言葉が使えるかの確かめ")
class PasswordLinkVerifierApiTest extends AuthApiTestBase {

	private static final String PREFIX = "http://localhost:8080/password-set?token=";

	@Autowired
	PasswordLinkVerifier verifier;

	@Autowired
	PasswordLinkIssuer issuer;

	@Autowired
	Clock clock;

	private String issue(String email, PasswordLinkPurpose purpose) {
		return issuer.issue(userId(email), purpose).getLinkUrl().substring(PREFIX.length());
	}

	private void assertUnusable(String token) {
		assertThatThrownBy(() -> verifier.verify(token))
				.isInstanceOf(BusinessRuleException.class)
				.hasMessage("このリンクは使えません。有効期限が切れたか、すでに使われています。");
	}

	@Test
	@DisplayName("作ったばかりのリンク：使える。リンクの行・ユーザ・用途を返す（用途ごと）")
	void usable() {
		for (PasswordLinkPurpose purpose : PasswordLinkPurpose.values()) {
			String token = issue(IPPAN, purpose);

			VerifiedPasswordLink link = verifier.verify(token);

			assertThat(link.getPurpose()).isEqualTo(purpose);
			assertThat(link.getUser().getEmail()).isEqualTo(IPPAN);
			assertThat(link.getPasswordToken().getTokenHash()).isEqualTo(PasswordLinkTokens.hash(token));
		}
	}

	@Test
	@DisplayName("確かめるだけでは何も書き換えない（used_at・revoked_at は空のまま。何度でも使える）")
	void doesNotWrite() {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);

		verifier.verify(token);
		verifier.verify(token);

		Map<String, Object> row = jdbc.queryForMap("SELECT used_at, revoked_at FROM password_tokens WHERE user_id = ?",
				userId(IPPAN));
		assertThat(row.get("used_at")).isNull();
		assertThat(row.get("revoked_at")).isNull();
	}

	@Test
	@DisplayName("空・ない合言葉：使えない")
	void blank() {
		assertUnusable(null);
		assertUnusable("");
		assertUnusable("   ");
	}

	@Test
	@DisplayName("データベースにない合言葉：使えない")
	void unknown() {
		issue(IPPAN, PasswordLinkPurpose.RESET);

		assertUnusable(PasswordLinkTokens.generate());
	}

	@Test
	@DisplayName("使用済み（used_at が入っている）：使えない")
	void used() {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);
		change("UPDATE password_tokens SET used_at = " + NOW + " WHERE user_id = ?", userId(IPPAN));

		assertUnusable(token);
	}

	@Test
	@DisplayName("より新しいリンクが作られた（前のリンクは revoked_at が入る）：前のリンクは使えず、新しいリンクは使える")
	void revokedByNewerLink() {
		String older = issue(IPPAN, PasswordLinkPurpose.RESET);
		String newer = issue(IPPAN, PasswordLinkPurpose.RESET);

		assertUnusable(older);
		assertThat(verifier.verify(newer).getUser().getEmail()).isEqualTo(IPPAN);
	}

	@Test
	@DisplayName("期限切れ：使えない（期限の1秒後）。期限の1分前なら使える（境界値）")
	void expired() {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);
		LocalDateTime now = LocalDateTime.now(clock);

		change("UPDATE password_tokens SET expires_at = ? WHERE user_id = ?", now.minusSeconds(1), userId(IPPAN));
		assertUnusable(token);

		change("UPDATE password_tokens SET expires_at = ? WHERE user_id = ?", now.plusMinutes(1), userId(IPPAN));
		assertThat(verifier.verify(token).getUser().getEmail()).isEqualTo(IPPAN);
	}

	@Test
	@DisplayName("削除されたユーザのリンク：使えない")
	void deletedUser() {
		String token = issue(IPPAN, PasswordLinkPurpose.RESET);
		change("UPDATE users SET deleted_at = " + NOW + " WHERE email = ?", IPPAN);

		assertUnusable(token);
	}

	@Test
	@DisplayName("ほかの人のリンクの合言葉でも、その人のリンクとして確かめる")
	void otherUsersLink() {
		issue(IPPAN, PasswordLinkPurpose.RESET);
		String soumuToken = issue(SOUMU, PasswordLinkPurpose.RESET);

		assertThat(verifier.verify(soumuToken).getUser().getEmail()).isEqualTo(SOUMU);
	}
}
