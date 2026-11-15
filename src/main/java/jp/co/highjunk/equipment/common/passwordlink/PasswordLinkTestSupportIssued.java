package jp.co.highjunk.equipment.common.passwordlink;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * テスト用 API（POST /api/test-support/password-links）が返す、作ったリンクの値（詳細設計 common.md 3.14）。
 */
@Getter
@RequiredArgsConstructor
public class PasswordLinkTestSupportIssued {

	/** リンクの合言葉（画面のテストが S-03 を開くときに使う） */
	private final String token;

	/** 用途（RESET・REGISTER・LOCK） */
	private final String purpose;

	/** 有効期限（日本時間。ISO 8601） */
	private final String expiresAt;
}
