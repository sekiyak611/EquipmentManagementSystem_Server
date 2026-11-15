package jp.co.highjunk.equipment.api.auth.passwordtokens.verify.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A-05 で画面に返す値（詳細設計 auth/A-05_合言葉の確認.md の3.1）。
 */
@Getter
@AllArgsConstructor
public class AuthPasswordTokensVerifyRestOutDto {

	/** 対象のメールアドレス。画面に表示するだけ（画面設計 6章） */
	private final String email;

	/** リンクの用途（RESET・LOCK・REGISTER）。REGISTER のときだけ、見出しを「パスワードの設定」にする */
	private final String purpose;
}
