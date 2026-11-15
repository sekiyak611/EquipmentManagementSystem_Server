package jp.co.highjunk.equipment.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 使えないパスワードの照らし合わせ（画面設計 8章。詳細設計 common.md 3.15）の単体テスト。
 */
class PasswordBlocklistTest {

	private final PasswordBlocklist blocklist = new PasswordBlocklist();

	private static final String EMAIL = "yamada@example.com";

	@Test
	@DisplayName("よく使われるパスワード（一覧にあるもの）は使えない")
	void commonPasswords() {
		assertThat(blocklist.isBlocked("password", EMAIL)).isTrue();
		assertThat(blocklist.isBlocked("12345678", EMAIL)).isTrue();
		assertThat(blocklist.isBlocked("qwertyuiop", EMAIL)).isTrue();
	}

	@Test
	@DisplayName("大文字・小文字は区別しない")
	void ignoresCase() {
		assertThat(blocklist.isBlocked("PASSWORD", EMAIL)).isTrue();
		assertThat(blocklist.isBlocked("PassWord", EMAIL)).isTrue();
		assertThat(blocklist.isBlocked("Yamada@Example.COM", EMAIL)).isTrue();
		assertThat(blocklist.isBlocked("EquipmentManagement", EMAIL)).isTrue();
	}

	@Test
	@DisplayName("そのユーザのメールアドレスと同じは使えない。ほかの人のメールアドレスは、そのユーザには使える")
	void ownEmail() {
		assertThat(blocklist.isBlocked(EMAIL, EMAIL)).isTrue();
		assertThat(blocklist.isBlocked("suzuki.taro@example.com", EMAIL)).isFalse();
	}

	@Test
	@DisplayName("システム名（equipment・equipmentmanagement・equipmentmanagementsystem）は使えない")
	void systemNames() {
		assertThat(blocklist.isBlocked("equipment", EMAIL)).isTrue();
		assertThat(blocklist.isBlocked("equipmentmanagement", EMAIL)).isTrue();
		assertThat(blocklist.isBlocked("equipmentmanagementsystem", EMAIL)).isTrue();
	}

	@Test
	@DisplayName("一覧にもメールアドレスにもシステム名にもない（同じでない）パスワードは使える。一部が含まれるだけなら使える")
	void allowed() {
		assertThat(blocklist.isBlocked("Xq7#kd9!Lm2", EMAIL)).isFalse();
		assertThat(blocklist.isBlocked("password-Xq7#kd9!", EMAIL)).isFalse();
		assertThat(blocklist.isBlocked("equipment-2026-Xq7", EMAIL)).isFalse();
	}

	@Test
	@DisplayName("メールアドレスがない（null）でも、一覧とシステム名は照らし合わせる")
	void nullEmail() {
		assertThat(blocklist.isBlocked("password", null)).isTrue();
		assertThat(blocklist.isBlocked("Xq7#kd9!Lm2", null)).isFalse();
	}
}
