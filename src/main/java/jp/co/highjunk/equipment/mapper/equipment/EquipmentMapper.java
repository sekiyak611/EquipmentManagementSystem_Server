package jp.co.highjunk.equipment.mapper.equipment;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.equipment.model.Equipment;
import jp.co.highjunk.equipment.equipment.model.EquipmentSearchCondition;

/**
 * equipment 機能の equipment テーブルの読み書き（詳細設計 equipment/equipment.md 2.1）。
 * category 機能の mapper.category.EquipmentMapper とは別のもの（同じ名前だが、部品の名前はパッケージ込み。common.md 2.3）。
 * SQL は src/main/resources の同じパッケージの場所の EquipmentMapper.xml に書く。
 */
@Mapper
public interface EquipmentMapper {

	/** 条件に合う、削除されていない備品の数（C-01 の7章 No.3）。 */
	long countBySearch(EquipmentSearchCondition condition);

	/** 条件に合う、削除されていない備品を、並び順で limit 件まで読む（C-01 の7章 No.4）。 */
	List<Equipment> selectBySearch(EquipmentSearchCondition condition);

	/** 削除されていない備品で、管理番号が同じものの数（C-03 の7章 No.2）。 */
	long countByManagementNo(@Param("managementNo") String managementNo);

	/** 備品を1つ足す。足した行の ID を equipment の id に入れる（C-03 の7章 No.3）。 */
	void insert(Equipment equipment);
}
