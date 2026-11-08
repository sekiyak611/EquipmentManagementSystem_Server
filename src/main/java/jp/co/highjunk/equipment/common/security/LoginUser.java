package jp.co.highjunk.equipment.common.security;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * セッションに入れる「ログインしている人」（詳細設計 common.md 3.10）。
 * ユーザIDと、ログインした日時だけを持つ。氏名や権限項目は持たない（変わることがあるので、使うたびに読む）。
 * ほかの機能の controller では、`@AuthenticationPrincipal LoginUserId user` で受け取って、ユーザIDを使う。
 *
 * @param userId      ログインしている人のユーザID（users.id）
 * @param loggedInAt  ログインした日時（日本時間）。users.session_valid_after と比べる（docs/API設計.md 2.2）
 */
public record LoginUser(long userId, LocalDateTime loggedInAt) implements LoginUserId, Serializable {

	@Override
	public long getUserId() {
		return userId;
	}
}
