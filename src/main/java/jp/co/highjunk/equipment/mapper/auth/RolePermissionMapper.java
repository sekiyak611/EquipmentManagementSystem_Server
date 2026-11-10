package jp.co.highjunk.equipment.mapper.auth;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * auth 機能の role_permissions テーブルの読み書き（詳細設計 auth/auth.md 2.1）。
 * SQL は src/main/resources の同じパッケージの場所の RolePermissionMapper.xml に書く。
 */
@Mapper
public interface RolePermissionMapper {

	/** 権限に含まれる権限項目のコード（並び順は決まらない）。 */
	List<String> selectPermissionCodesByRoleId(@Param("roleId") long roleId);
}
