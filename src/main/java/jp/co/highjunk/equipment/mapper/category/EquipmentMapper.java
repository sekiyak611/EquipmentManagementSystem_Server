package jp.co.highjunk.equipment.mapper.category;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

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
}
