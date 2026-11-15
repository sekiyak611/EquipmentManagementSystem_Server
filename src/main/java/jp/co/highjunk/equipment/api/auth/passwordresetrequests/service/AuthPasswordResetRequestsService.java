package jp.co.highjunk.equipment.api.auth.passwordresetrequests.service;

/**
 * A-04 パスワード設定用のリンクの依頼の業務の処理（中身は AuthPasswordResetRequestsServiceImpl。
 * 詳細設計 auth/A-04_パスワード設定リンクの依頼.md）。
 */
public interface AuthPasswordResetRequestsService {

	/**
	 * 依頼されたメールアドレスのユーザに、パスワード設定用のリンクをDMで送る。
	 * 登録がない・5分以内・DMが送れないときも、例外にしない（呼んだ側は、いつも同じ返し方をする）。
	 */
	void request(AuthPasswordResetRequestsServiceInDto serviceInDto);
}
