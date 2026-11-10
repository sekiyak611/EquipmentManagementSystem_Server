package jp.co.highjunk.equipment.mapper.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * auth 機能の roles テーブルの読み書き（詳細設計 auth/auth.md 2.1）。
 * SQL は src/main/resources の同じパッケージの場所の RoleMapper.xml に書く。
 */
@Mapper
public interface RoleMapper {

	/** 権限の名前。 */
	String selectNameById(@Param("id") long id);
}
