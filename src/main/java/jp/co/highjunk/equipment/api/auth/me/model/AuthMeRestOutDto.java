package jp.co.highjunk.equipment.api.auth.me.model;

import java.util.List;

import jp.co.highjunk.equipment.common.constant.HomePage;
import jp.co.highjunk.equipment.common.security.LoginUserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A-03 で返す data の中身（docs/API設計.md 4.2）。
 */
@Getter
@AllArgsConstructor
public class AuthMeRestOutDto {

	/** ユーザID */
	private final long id;

	/** 氏名 */
	private final String name;

	/** メールアドレス */
	private final String email;

	/** 権限の名前 */
	private final String roleName;

	/** 権限項目のコード（権限項目の並び順） */
	private final List<String> permissions;

	/** ログインしたあと最初に出す画面 */
	private final HomePage homePage;

	public static AuthMeRestOutDto from(LoginUserInfo info) {
		return new AuthMeRestOutDto(info.getId(), info.getName(), info.getEmail(), info.getRoleName(),
				info.getPermissions(), info.getHomePage());
	}
}
