package jp.co.highjunk.equipment.common.security;

/**
 * ログインしている人のユーザID（users.id）を持つもの。
 * ログイン（ZPOP-25）で作る「ログインしている人」のクラスに付けると、ログにユーザIDが出る（UserIdLogFilter）。
 */
public interface LoginUserId {

	/** ログインしている人のユーザID（users.id） */
	long getUserId();
}
