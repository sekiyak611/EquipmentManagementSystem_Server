package jp.co.highjunk.equipment.dao.mapper.custom;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.dao.entity.Loan;

/**
 * loans の、MyBatis Generator では作れない SQL（手で書く Mapper。CLAUDE.md 4.1・5章）。
 * SQL は src/main/resources/jp/co/highjunk/equipment/dao/mapper/custom/LoanCustomMapper.xml に書く。
 */
@Mapper
public interface LoanCustomMapper {

	/**
	 * 取り消されていない貸出履歴を1つ読み、行を押さえる（FOR UPDATE）。確かめてから書き換えるまでの間に、ほかの人に変えられないようにする
	 * （B-03 の8章。詳細設計 common.md 3.20）。ない・取り消し済みなら null。Example では FOR UPDATE を書けないので、手で書く。
	 */
	Loan selectByIdForUpdate(@Param("id") long id);
}
