package jp.co.highjunk.equipment.dto.auth;

import java.util.List;

import jp.co.highjunk.equipment.common.constant.HomePage;

/**
 * A-01・A-03 で返す data の中身（docs/API設計.md 4.2）。
 *
 * @param id          ユーザID
 * @param name        氏名
 * @param email       メールアドレス
 * @param roleName    権限の名前
 * @param permissions 権限項目のコード（権限項目の並び順）
 * @param homePage    ログインしたあと最初に出す画面
 */
public record MeResponse(long id, String name, String email, String roleName, List<String> permissions,
		HomePage homePage) {
}
