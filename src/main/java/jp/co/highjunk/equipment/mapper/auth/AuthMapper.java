package jp.co.highjunk.equipment.mapper.auth;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.auth.model.User;

/**
 * auth 機能のデータベースの読み書き（詳細設計 auth/auth.md 2.1）。
 * ほかの機能の mapper と Spring の部品の名前がぶつからないよう、機能の名前を付けた。
 * SQL は src/main/resources の同じパッケージの場所の AuthMapper.xml に書く。
 */
@Mapper
public interface AuthMapper {

	/** 削除されていないユーザを、メールアドレス（小文字にそろえたもの）で探す。いなければ null。 */
	User selectUserByEmail(@Param("email") String email);

	/** 削除されていないユーザを、ID で探す。いなければ null。 */
	User selectUserById(@Param("id") long id);

	/** 権限の名前。 */
	String selectRoleName(@Param("roleId") long roleId);

	/** 権限に含まれる権限項目のコード（並び順は決まらない）。 */
	List<String> selectPermissionCodes(@Param("roleId") long roleId);

	/** 渡した権限項目のコードを、権限項目の並び順（sort_order）に並べて返す。codes は1件以上。 */
	List<String> selectSortedPermissionCodes(@Param("codes") List<String> codes);
}
