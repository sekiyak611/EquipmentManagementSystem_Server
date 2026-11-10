package jp.co.highjunk.equipment.mapper.category;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.category.model.Category;

/**
 * category 機能の categories テーブルの読み書き（詳細設計 category/category.md 2.1、K-01〜K-03 の7章）。
 * SQL は src/main/resources の同じパッケージの場所の CategoryMapper.xml に書く。
 */
@Mapper
public interface CategoryMapper {

	/** 削除されていない種別を、頭文字の順（頭文字がないものは後ろに名前の順）で全部読む。 */
	List<Category> selectAll();

	/** 削除されていない種別を ID で読む。なければ null。 */
	Category selectById(@Param("id") long id);

	/**
	 * 削除されていない種別のうち、名前が同じものの数（照合順序 utf8mb4_ja_0900_as_cs_ks で比べる）。
	 * excludeId を渡すと、その種別は数えない（K-03 で自分自身を除く）。
	 */
	int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

	/** 削除されていない種別のうち、頭文字が同じものの数。excludeId を渡すと、その種別は数えない。 */
	int countByInitial(@Param("initial") String initial, @Param("excludeId") Long excludeId);

	/**
	 * 種別を1つ足す。作った ID は category の id に入る。version は初期値の 0。
	 *
	 * @throws org.springframework.dao.DuplicateKeyException 名前・頭文字が、削除されていないほかの種別と重なるとき
	 */
	void insert(Category category);

	/**
	 * 種別の名前・頭文字を変え、version を1増やす。id と version（画面を開いたときの値）が同じで、削除されていない行だけを変える。
	 *
	 * @return 変えた件数（0 なら、ほかの人が先に変えた・消した）
	 * @throws org.springframework.dao.DuplicateKeyException 名前・頭文字が、削除されていないほかの種別と重なるとき
	 */
	int update(Category category);
}
