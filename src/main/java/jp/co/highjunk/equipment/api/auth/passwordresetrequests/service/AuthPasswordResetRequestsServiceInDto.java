package jp.co.highjunk.equipment.api.auth.passwordresetrequests.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A-04 で Service に渡す値（詳細設計 auth/A-04_パスワード設定リンクの依頼.md の2.5）。Controller が RestInDto から作る。
 * 受け取った値だけで DB と照らして確かめることはない。
 */
@Getter
@AllArgsConstructor
public class AuthPasswordResetRequestsServiceInDto {

	/** メールアドレス（受け取ったまま。小文字にそろえるのは ServiceImpl） */
	private final String email;
}
