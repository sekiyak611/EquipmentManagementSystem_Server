package jp.co.highjunk.equipment.mapper.equipment;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.equipment.model.Category;

/**
 * equipment 機能の categories テーブルの読み書き（読むだけ。詳細設計 equipment/equipment.md 2.1）。
 * category 機能の mapper.category.CategoryMapper とは別のもの。
 */
@Mapper
public interface CategoryMapper {

	/** 種別をIDでまとめて読む（削除済みも読む。C-01 の7章 No.5）。ids は空にしない。 */
	List<Category> selectByIds(@Param("ids") Collection<Long> ids);
}
