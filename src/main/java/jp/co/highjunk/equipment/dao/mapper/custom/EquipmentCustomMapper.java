package jp.co.highjunk.equipment.dao.mapper.custom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.dao.entity.custom.CategoryEquipmentCount;

/**
 * equipment の、MyBatis Generator では作れない SQL（手で書く Mapper。読むだけ。CLAUDE.md 4.1・5章）。
 * SQL は src/main/resources/jp/co/highjunk/equipment/dao/mapper/custom/EquipmentCustomMapper.xml に書く。
 */
@Mapper
public interface EquipmentCustomMapper {

	/**
	 * 削除されていない備品の数を、種別ごとに数える（管理状態は問わない）。備品のない種別は結果に出ない。
	 * K-01 で、種別ごとに SQL を出さずに1回で数えるため（N+1 を防ぐ）。Example では GROUP BY を書けないので、手で書く。
	 */
	List<CategoryEquipmentCount> countGroupByCategoryId();

	/**
	 * 1つの種別の、削除されていない備品の数。ほかの人が確定させたばかりの行も数え、数えた行を押さえる（FOR SHARE）。
	 * K-04 で、使っている備品がないかを確かめるため（詳細設計 equipment/category/K-04_種別を消す.md の8章）。
	 */
	long countByCategoryIdForShare(@Param("categoryId") long categoryId);
}
