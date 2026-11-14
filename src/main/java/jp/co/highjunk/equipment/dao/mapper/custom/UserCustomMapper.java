package jp.co.highjunk.equipment.dao.mapper.custom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * users の、MyBatis Generator では作れない SQL（手で書く Mapper。読むだけ。CLAUDE.md 4.1・5章）。
 * SQL は src/main/resources/jp/co/highjunk/equipment/dao/mapper/custom/UserCustomMapper.xml に書く。
 */
@Mapper
public interface UserCustomMapper {

	/**
	 * 氏名にキーワードを含むユーザのID（削除済みも含める。C-01 の7章）。
	 * 大文字・小文字を区別せずに探すため LOWER を使い、LIKE の特別な文字を ! で打ち消す。Example では書けないので、手で書く。
	 *
	 * @param keywordPattern LIKE の形（%小文字のキーワード%。% _ ! には ! を付けたもの）
	 */
	List<Long> selectIdsByNameContaining(@Param("keywordPattern") String keywordPattern);
}
