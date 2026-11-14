package jp.co.highjunk.equipment.dao.mapper.custom;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.dao.entity.Category;

/**
 * categories の、MyBatis Generator では作れない SQL（手で書く Mapper。CLAUDE.md 4.1・5章）。
 * SQL は src/main/resources/jp/co/highjunk/equipment/dao/mapper/custom/CategoryCustomMapper.xml に書く。
 */
@Mapper
public interface CategoryCustomMapper {

	/**
	 * 削除されていない種別を ID で読み、まとまり（トランザクション）が終わるまで、その行をほかの人が変えられないように押さえる（FOR UPDATE）。
	 * なければ null。K-04 で、確かめから消すまでの間に、その種別の備品を足されないようにするため（詳細設計 equipment/category/K-04_種別を消す.md の8章）。
	 * Example では FOR UPDATE を書けないので、手で書く。
	 */
	Category selectByIdForUpdate(@Param("id") long id);

	/**
	 * 削除されていない種別を ID で読み、行を押さえる（FOR SHARE）。備品を足すとき（C-03）、K-04 で同じ種別を同時に消されないようにする
	 * （詳細設計 equipment/category/category.md 3章の No.2）。ない・削除済みなら null。
	 */
	Category selectByIdForShare(@Param("id") long id);
}
