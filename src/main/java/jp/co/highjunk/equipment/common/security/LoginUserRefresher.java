package jp.co.highjunk.equipment.common.security;

import java.util.List;
import java.util.Optional;

/**
 * APIを呼ぶたびに、ログインしている人の今の権限項目を返す約束（詳細設計 common.md 3.10）。
 * 中身は auth 機能が持つ（common から機能のパッケージを使わないため、約束だけをここに置く。CLAUDE.md 5章）。
 */
public interface LoginUserRefresher {

	/**
	 * ログインしている人の、今の権限項目のコードを返す。
	 *
	 * @param loginUser セッションに入っている、ログインしている人
	 * @return 権限項目のコード。ログアウトさせるとき（ユーザが削除された、ログインのあとにパスワードが変わった）は空
	 */
	Optional<List<String>> currentPermissions(LoginUser loginUser);
}
