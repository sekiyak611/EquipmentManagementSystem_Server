package jp.co.highjunk.equipment.common.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * パスワードが、使えないものと同じかを調べる（画面設計 8章。詳細設計 common.md 3.15）。A-06・A-07 の Validator が使う。
 * 使えないもの：そのユーザのメールアドレス、システム名、よく使われるパスワードの一覧（SecLists の 10k-most-common.txt。MIT）。
 * 大文字・小文字は区別しない。一覧は起動時に1回だけ読み込む。読み込めなければ、起動を止める（弱いパスワードを通さないため）。
 */
@Component
public class PasswordBlocklist {

	/** よく使われるパスワードの一覧（クラスパス上のファイル。外部のサービスには問い合わせない） */
	static final String COMMON_PASSWORDS_FILE = "password/10k-most-common.txt";

	/** システム名（半角。小文字）。「備品管理」は半角でないので、文字の決まりで先に断られる（A-06 の12章 No.6） */
	private static final Set<String> SYSTEM_NAMES = Set.of("equipment", "equipmentmanagement",
			"equipmentmanagementsystem");

	private final Set<String> commonPasswords;

	public PasswordBlocklist() {
		this.commonPasswords = load();
	}

	/**
	 * 使えないパスワードか。
	 *
	 * @param password 新しいパスワード
	 * @param email    そのユーザのメールアドレス
	 */
	public boolean isBlocked(String password, String email) {
		String lower = password.toLowerCase(Locale.ROOT);
		return (email != null && lower.equals(email.toLowerCase(Locale.ROOT)))
				|| SYSTEM_NAMES.contains(lower)
				|| commonPasswords.contains(lower);
	}

	private static Set<String> load() {
		Set<String> passwords = new HashSet<>();
		try (InputStream in = new ClassPathResource(COMMON_PASSWORDS_FILE).getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String entry = line.strip().toLowerCase(Locale.ROOT);
				if (!entry.isEmpty()) {
					passwords.add(entry);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("よく使われるパスワードの一覧を読み込めません（" + COMMON_PASSWORDS_FILE + "）", e);
		}
		if (passwords.isEmpty()) {
			throw new IllegalStateException("よく使われるパスワードの一覧が空です（" + COMMON_PASSWORDS_FILE + "）");
		}
		return Set.copyOf(passwords);
	}
}
