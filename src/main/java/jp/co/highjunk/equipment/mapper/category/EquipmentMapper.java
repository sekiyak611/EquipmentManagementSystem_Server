package jp.co.highjunk.equipment.mapper.category;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.category.model.CategoryEquipmentCount;

/**
 * category 機能の equipment テーブルの読み書き（読むだけ。詳細設計 category/category.md 2.1）。
 * equipment 機能の mapper.equipment.EquipmentMapper とは別のもの（同じ名前だが、部品の名前はパッケージ込み。common.md 2.3）。
 * SQL は src/main/resources の同じパッケージの場所の EquipmentMapper.xml に書く。
 */
@Mapper
public interface EquipmentMapper {

	/** 削除されていない備品の数を、種別ごとに数える。備品のない種別は結果に出ない。 */
	List<CategoryEquipmentCount> countGroupByCategoryId();

	/** 1つの種別の、削除されていない備品の数（管理状態は問わない）。 */
	long countByCategoryId(@Param("categoryId") long categoryId);

	/**
	 * 1つの種別の、削除されていない備品の数。ほかの人が確定させたばかりの行も数え、数えた行を押さえる（FOR SHARE）。
	 * K-04 で、使っている備品がないかを確かめるため（K-04 の8章）。
	 */
	long countByCategoryIdForShare(@Param("categoryId") long categoryId);
}
