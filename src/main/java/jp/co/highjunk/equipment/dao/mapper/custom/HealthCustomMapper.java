package jp.co.highjunk.equipment.dao.mapper.custom;

import org.apache.ibatis.annotations.Mapper;

/**
 * データベースにつながるかだけを確かめる SQL（A-08。手で書く Mapper。CLAUDE.md 4.1）。
 * テーブルを使わないので、名前を `<テーブルの形>CustomMapper` にできない。テーブルを使わない Mapper だけの例外（CLAUDE.md 5章の命名規則）。
 * SQL は src/main/resources/jp/co/highjunk/equipment/dao/mapper/custom/HealthCustomMapper.xml に書く。
 */
@Mapper
public interface HealthCustomMapper {

	/** データベースにつながるかだけを確かめる（何も読まない）。 */
	int selectOne();
}
