package jp.co.highjunk.equipment.mapper.auth;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * auth 機能の permissions テーブルの読み書き（詳細設計 auth/auth.md 2.1）。
 * SQL は src/main/resources の同じパッケージの場所の PermissionMapper.xml に書く。
 */
@Mapper
public interface PermissionMapper {

	/** 渡した権限項目のコードを、権限項目の並び順（sort_order）に並べて返す。codes は1件以上。 */
	List<String> selectSortedCodes(@Param("codes") List<String> codes);
}
