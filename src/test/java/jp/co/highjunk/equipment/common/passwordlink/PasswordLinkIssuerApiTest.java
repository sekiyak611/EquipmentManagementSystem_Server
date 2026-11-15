package jp.co.highjunk.equipment.common.passwordlink;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;

/**
 * パスワード設定用のリンクを作る部品（詳細設計 common.md 3.14）のテスト。データベースを使う（テストのあとに元に戻す）。
 */
@DisplayName("common.passwordlink パスワード設定用のリンクを作る")
class PasswordLinkIssuerApiTest extends AuthApiTestBase {

	private static final String PREFIX = "http://localhost:8080/password-set?token=";

	@Autowired
	PasswordLinkIssuer issuer;

	private String tokenOf(IssuedPasswordLink link) {
		assertThat(link.getLinkUrl()).startsWith(PREFIX);
		return link.getLinkUrl().substring(PREFIX.length());
	}

	private List<Map<String, Object>> rows(long userId) {
		return jdbc.queryForList("SELECT * FROM password_tokens WHERE user_id = ? ORDER BY id", userId);
	}

	private long minutesToExpire(Map<String, Object> row) {
		return jdbc.queryForObject(
				"SELECT TIMESTAMPDIFF(MINUTE, created_at, expires_at) FROM password_tokens WHERE id = ?",
				Long.class, row.get("id"));
	}

	@Test
	@DisplayName("リンクは <app.base-url>/password-set?token=<合言葉（43文字）>")
	void linkUrl() {
		IssuedPasswordLink link = issuer.issue(userId(IPPAN), PasswordLinkPurpose.LOCK);

		assertThat(tokenOf(link)).matches("[A-Za-z0-9_-]{43}");
		assertThat(link.getPurpose()).isEqualTo(PasswordLinkPurpose.LOCK);
	}

	@Test
	@DisplayName("データベースに入るのはハッシュだけ。合言葉そのものはどの列にも入らない")
	void storesOnlyHash() {
		long userId = userId(IPPAN);
		IssuedPasswordLink link = issuer.issue(userId, PasswordLinkPurpose.RESET);

		String token = tokenOf(link);
		List<Map<String, Object>> rows = rows(userId);
		assertThat(rows).hasSize(1);
		Map<String, Object> row = rows.get(0);
		assertThat(row.get("token_hash")).isEqualTo(PasswordLinkTokens.hash(token));
		assertThat(row.get("purpose")).isEqualTo("RESET");
		assertThat(row.get("used_at")).isNull();
		assertThat(row.get("revoked_at")).isNull();
		assertThat(row.values().stream().map(String::valueOf)).noneMatch(v -> v.contains(token));
	}

	@Test
	@DisplayName("有効期限：RESET・LOCK は30分、REGISTER は3日（作った日時から）")
	void validityByPurpose() {
		long userId = userId(IPPAN);
		issuer.issue(userId, PasswordLinkPurpose.RESET);
		issuer.issue(userId, PasswordLinkPurpose.LOCK);
		issuer.issue(userId, PasswordLinkPurpose.REGISTER);

		List<Map<String, Object>> rows = rows(userId);
		assertThat(rows).hasSize(3);
		assertThat(minutesToExpire(rows.get(0))).isEqualTo(30);
		assertThat(minutesToExpire(rows.get(1))).isEqualTo(30);
		assertThat(minutesToExpire(rows.get(2))).isEqualTo(3 * 24 * 60);
	}

	@Test
	@DisplayName("新しく作ると、その人の前のリンクは無効になる（用途が違っても）。新しいリンクは有効のまま")
	void revokesPreviousLinks() {
		long userId = userId(IPPAN);
		issuer.issue(userId, PasswordLinkPurpose.RESET);
		issuer.issue(userId, PasswordLinkPurpose.LOCK);

		List<Map<String, Object>> rows = rows(userId);
		assertThat(rows).hasSize(2);
		assertThat(rows.get(0).get("revoked_at")).isNotNull();
		assertThat(rows.get(1).get("revoked_at")).isNull();
	}

	@Test
	@DisplayName("使い終わったリンクの revoked_at は書き換えない。ほかの人のリンクも変えない")
	void keepsUsedAndOthers() {
		long ippan = userId(IPPAN);
		long soumu = userId(SOUMU);
		issuer.issue(ippan, PasswordLinkPurpose.RESET);
		issuer.issue(soumu, PasswordLinkPurpose.RESET);
		change("UPDATE password_tokens SET used_at = " + NOW + " WHERE user_id = ?", ippan);

		issuer.issue(ippan, PasswordLinkPurpose.LOCK);

		List<Map<String, Object>> ippanRows = rows(ippan);
		assertThat(ippanRows.get(0).get("revoked_at")).isNull();
		assertThat(ippanRows.get(0).get("used_at")).isNotNull();
		assertThat(rows(soumu).get(0).get("revoked_at")).isNull();
	}

	@Test
	@DisplayName("合言葉は毎回違い、ハッシュも重ならない")
	void differentEachTime() {
		long userId = userId(IPPAN);
		IssuedPasswordLink first = issuer.issue(userId, PasswordLinkPurpose.LOCK);
		IssuedPasswordLink second = issuer.issue(userId, PasswordLinkPurpose.LOCK);

		assertThat(tokenOf(first)).isNotEqualTo(tokenOf(second));
		assertThat(rows(userId)).hasSize(2);
	}
}
