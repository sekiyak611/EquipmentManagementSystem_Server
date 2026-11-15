package jp.co.highjunk.equipment.api.auth.passwordtokens.use.service;

import java.time.LocalDateTime;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.dao.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * パスワードを決めた結果（AuthPasswordTokensUsePasswordSetter が返す）。確定したあとのDM（④）に使う。
 */
@Getter
@RequiredArgsConstructor
public class AuthPasswordTokensUsePasswordSetResult {

	/** パスワードを決めたユーザ */
	private final User user;

	/** 使ったリンクの用途（REGISTER のときは、DMを送らない） */
	private final PasswordLinkPurpose purpose;

	/** パスワードが変わった日時（日本時間。Clock から取ったもの） */
	private final LocalDateTime changedAt;
}
