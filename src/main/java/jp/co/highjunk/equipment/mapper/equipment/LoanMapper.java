package jp.co.highjunk.equipment.mapper.equipment;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.equipment.model.Loan;

/**
 * equipment 機能の loans テーブルの読み書き（詳細設計 equipment/equipment.md 2.1）。
 */
@Mapper
public interface LoanMapper {

	/** 貸出中の貸出履歴（返却日が空で、取り消されていない行）をすべて読む（C-01 の7章 No.1。データ設計 5.7）。 */
	List<Loan> selectActive();

	/** 1つの備品の、貸出中の貸出履歴（C-06 の7章 No.4）。貸出中でなければ null。 */
	Loan selectActiveByEquipmentId(@Param("equipmentId") long equipmentId);
}
