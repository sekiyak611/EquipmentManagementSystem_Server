package jp.co.highjunk.equipment.common.passwordlink;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * パスワード設定用のリンクの合言葉（トークン）を作る・ハッシュにする（詳細設計 common.md 3.14）。
 * 合言葉を確かめる側（A-05・A-06）も、同じ {@link #hash(String)} を使う。
 */
public final class PasswordLinkTokens {

	/** 合言葉のもとになる乱数の長さ（バイト）。Base64URL にすると43文字。 */
	private static final int TOKEN_BYTES = 32;

	private static final SecureRandom RANDOM = new SecureRandom();

	private PasswordLinkTokens() {
	}

	/** 推測されにくい合言葉を作る（英数字と「-」「_」だけの43文字）。 */
	public static String generate() {
		byte[] bytes = new byte[TOKEN_BYTES];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	/** 合言葉のハッシュ（SHA-256 の16進数・小文字の64文字）。データベースにはこれを保存する。 */
	public static String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 は、どの Java にも必ずある
			throw new IllegalStateException(e);
		}
	}
}
