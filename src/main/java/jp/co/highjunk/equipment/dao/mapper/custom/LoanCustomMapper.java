package jp.co.highjunk.equipment.dao.mapper.custom;

import java.util.List;

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

	/**
	 * 備品の ID の一覧から、その備品を今借りている貸出履歴（返却日が空で、取り消されていない）を、まとめて読み、行を押さえる（FOR UPDATE）。
	 * 1つの備品に今借りている貸出履歴は多くて1件なので、結果は ID の数以下。結果は備品の ID の昇順。
	 * 条件は、返却日が空で取り消されていない行にだけ備品の ID が入る列（active_equipment_id。一意）にする
	 * （返却済みの行や、その前後の隙間まで押さえないため）。押さえる順は、この索引の順（備品の ID の昇順）で決まる（B-04 の8章）。
	 * equipmentIds が空・null なら、SQL は何も返さない（空の IN は書けないため）。Example では FOR UPDATE を書けないので、手で書く。
	 */
	List<Loan> selectCurrentByEquipmentIdsForUpdate(@Param("equipmentIds") List<Long> equipmentIds);
}
