package jp.co.highjunk.equipment.common.validation;

/**
 * 新しいパスワードと、その確認の値を持つもの（{@link PasswordConfirmed} を付ける RestInDto が実装する）。
 */
public interface PasswordConfirmable {

	/** 新しいパスワード */
	String getPassword();

	/** 新しいパスワード（確認） */
	String getPasswordConfirm();
}
