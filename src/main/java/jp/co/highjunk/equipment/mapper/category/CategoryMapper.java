package jp.co.highjunk.equipment.mapper.category;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.category.model.Category;

/**
 * category 機能の categories テーブルの読み書き（詳細設計 category/category.md 2.1、K-01・K-02 の7章）。
 * SQL は src/main/resources の同じパッケージの場所の CategoryMapper.xml に書く。
 */
@Mapper
public interface CategoryMapper {

	/** 削除されていない種別を、頭文字の順（頭文字がないものは後ろに名前の順）で全部読む。 */
	List<Category> selectAll();

	/** 削除されていない種別のうち、名前が同じものの数（照合順序 utf8mb4_ja_0900_as_cs_ks で比べる）。 */
	int countByName(@Param("name") String name);

	/** 削除されていない種別のうち、頭文字が同じものの数。 */
	int countByInitial(@Param("initial") String initial);

	/**
	 * 種別を1つ足す。作った ID は category の id に入る。version は初期値の 0。
	 *
	 * @throws org.springframework.dao.DuplicateKeyException 名前・頭文字が、削除されていないほかの種別と重なるとき
	 */
	void insert(Category category);
}
