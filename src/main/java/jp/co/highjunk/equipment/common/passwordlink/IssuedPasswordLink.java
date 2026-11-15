package jp.co.highjunk.equipment.common.passwordlink;

import java.time.LocalDateTime;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 作ったパスワード設定用のリンク（詳細設計 common.md 3.14）。DMに入れるために、呼んだ側に返す。
 * linkUrl には合言葉が入っているので、ログに出さない。
 */
@Getter
@RequiredArgsConstructor
public class IssuedPasswordLink {

	/** DMに入れるアドレス（合言葉を含む）。 */
	private final String linkUrl;

	/** 用途。 */
	private final PasswordLinkPurpose purpose;

	/** 有効期限の日時。 */
	private final LocalDateTime expiresAt;
}
