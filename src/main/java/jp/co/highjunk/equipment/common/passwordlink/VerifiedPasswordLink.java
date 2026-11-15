package jp.co.highjunk.equipment.common.passwordlink;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.dao.entity.PasswordToken;
import jp.co.highjunk.equipment.dao.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 確かめて、使えるとわかったパスワード設定用のリンク（詳細設計 common.md 3.14）。
 * リンクの行・ユーザ・用途を持つ。token_hash を含むので、ログには出さない。
 */
@Getter
@RequiredArgsConstructor
public class VerifiedPasswordLink {

	/** リンクの行（password_tokens）。 */
	private final PasswordToken passwordToken;

	/** リンクを受け取る人（削除されていないユーザ）。 */
	private final User user;

	/** 用途。 */
	private final PasswordLinkPurpose purpose;
}
