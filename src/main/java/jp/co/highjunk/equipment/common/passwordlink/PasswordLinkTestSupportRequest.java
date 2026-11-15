package jp.co.highjunk.equipment.common.passwordlink;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import lombok.Getter;

/**
 * テスト用 API（POST /api/test-support/password-links）で受け取る値（詳細設計 common.md 3.14）。
 */
@Getter
public class PasswordLinkTestSupportRequest {

	/** リンクを作るユーザのメールアドレス */
	@NotBlank
	private final String email;

	/** 用途（RESET・REGISTER・LOCK） */
	@NotNull
	private final PasswordLinkPurpose purpose;

	@JsonCreator
	public PasswordLinkTestSupportRequest(@JsonProperty("email") String email,
			@JsonProperty("purpose") PasswordLinkPurpose purpose) {
		this.email = email;
		this.purpose = purpose;
	}
}
