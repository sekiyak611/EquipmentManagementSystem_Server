package jp.co.highjunk.equipment.common.security;

/**
 * ログインしている人のユーザID（users.id）を持つもの。
 * 「ログインしている人」のクラス（LoginUser）が持つ。ログにユーザIDが出る（UserIdLogFilter）。
 * ほかの機能の controller では、`@AuthenticationPrincipal LoginUserId user` で受け取る。
 */
public interface LoginUserId {

	/** ログインしている人のユーザID（users.id） */
	long getUserId();
}
