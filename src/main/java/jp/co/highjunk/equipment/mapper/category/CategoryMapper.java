package jp.co.highjunk.equipment.mapper.category;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import jp.co.highjunk.equipment.category.model.Category;

/**
 * category 機能の categories テーブルの読み書き（詳細設計 category/category.md 2.1）。
 * SQL は src/main/resources の同じパッケージの場所の CategoryMapper.xml に書く。
 */
@Mapper
public interface CategoryMapper {

	/** 削除されていない種別を、頭文字の順（頭文字がないものは後ろに名前の順）で全部読む。 */
	List<Category> selectAll();
}
