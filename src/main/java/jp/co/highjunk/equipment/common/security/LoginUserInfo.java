package jp.co.highjunk.equipment.common.security;

import java.util.List;

import jp.co.highjunk.equipment.common.constant.HomePage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ログインしている人の情報と権限項目（docs/API設計.md 4.2 の形）。LoginUserInfoAssembler が作り、A-01・A-03 が自分の RestOutDto に写して返す。
 */
@Getter
@AllArgsConstructor
public class LoginUserInfo {

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
}
