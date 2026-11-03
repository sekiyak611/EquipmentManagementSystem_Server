package jp.co.highjunk.equipment.health.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * health 機能のデータベースの読み書き。SQLは同じ場所の HealthMapper.xml に書く（CLAUDE.md 5章）。
 */
@Mapper
public interface HealthMapper {

	/** データベースにつながるかだけを確かめる（何も読まない）。 */
	int selectOne();
}
