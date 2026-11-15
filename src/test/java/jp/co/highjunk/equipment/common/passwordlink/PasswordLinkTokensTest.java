package jp.co.highjunk.equipment.common.passwordlink;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * パスワード設定用のリンクの合言葉を作る・ハッシュにする部品（詳細設計 common.md 3.14）の単体テスト。
 */
class PasswordLinkTokensTest {

	@Test
	@DisplayName("合言葉は43文字で、URL に使える文字（英数字・「-」「_」）だけ")
	void tokenShape() {
		for (int i = 0; i < 100; i++) {
			assertThat(PasswordLinkTokens.generate()).hasSize(43).matches("[A-Za-z0-9_-]{43}");
		}
	}

	@Test
	@DisplayName("合言葉は毎回違う（1000個で重ならない）")
	void tokensAreDifferent() {
		Set<String> tokens = new HashSet<>();
		for (int i = 0; i < 1000; i++) {
			tokens.add(PasswordLinkTokens.generate());
		}
		assertThat(tokens).hasSize(1000);
	}

	@Test
	@DisplayName("ハッシュは SHA-256 の16進数・小文字の64文字（\"abc\" の SHA-256 と同じ値になる）")
	void hashIsSha256Hex() {
		assertThat(PasswordLinkTokens.hash("abc"))
				.isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
		assertThat(PasswordLinkTokens.hash(PasswordLinkTokens.generate())).matches("[0-9a-f]{64}");
	}

	@Test
	@DisplayName("同じ合言葉なら同じハッシュ、違う合言葉なら違うハッシュ")
	void hashIsStable() {
		assertThat(PasswordLinkTokens.hash("token-a")).isEqualTo(PasswordLinkTokens.hash("token-a"));
		assertThat(PasswordLinkTokens.hash("token-a")).isNotEqualTo(PasswordLinkTokens.hash("token-b"));
	}

	@Test
	@DisplayName("ハッシュは合言葉そのものを含まない")
	void hashDoesNotContainToken() {
		String token = PasswordLinkTokens.generate();
		assertThat(PasswordLinkTokens.hash(token)).doesNotContain(token);
	}
}
