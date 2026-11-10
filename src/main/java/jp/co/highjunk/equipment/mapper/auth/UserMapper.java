package jp.co.highjunk.equipment.mapper.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.auth.model.User;

/**
 * auth 機能の users テーブルの読み書き（詳細設計 auth/auth.md 2.1）。
 * SQL は src/main/resources の同じパッケージの場所の UserMapper.xml に書く。
 */
@Mapper
public interface UserMapper {

	/** 削除されていないユーザを、メールアドレス（小文字にそろえたもの）で探す。いなければ null。 */
	User selectByEmail(@Param("email") String email);

	/** 削除されていないユーザを、ID で探す。いなければ null。 */
	User selectById(@Param("id") long id);
}
