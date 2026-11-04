package jp.co.highjunk.equipment.mapper.health;

import org.apache.ibatis.annotations.Mapper;

/**
 * health 機能のデータベースの読み書き。
 * mapper は機能のパッケージの外の mapper.<機能名> に置く（CLAUDE.md 4.1）。
 * SQLは src/main/resources の同じパッケージの場所の HealthMapper.xml に書く（CLAUDE.md 5章）。
 */
@Mapper
public interface HealthMapper {

	/** データベースにつながるかだけを確かめる（何も読まない）。 */
	int selectOne();
}
