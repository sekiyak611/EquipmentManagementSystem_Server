package jp.co.highjunk.equipment.dao.mapper.custom;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.custom.CategoryEquipmentCount;
import jp.co.highjunk.equipment.dao.entity.custom.EquipmentSearchCondition;

/**
 * equipment の、MyBatis Generator では作れない SQL（手で書く Mapper。CLAUDE.md 4.1・5章）。
 * SQL は src/main/resources/jp/co/highjunk/equipment/dao/mapper/custom/EquipmentCustomMapper.xml に書く。
 */
@Mapper
public interface EquipmentCustomMapper {

	/**
	 * 削除されていない備品の数を、種別ごとに数える（管理状態は問わない）。備品のない種別は結果に出ない。
	 * K-01 で、種別ごとに SQL を出さずに1回で数えるため（N+1 を防ぐ）。Example では GROUP BY を書けないので、手で書く。
	 */
	List<CategoryEquipmentCount> countGroupByCategoryId();

	/**
	 * 1つの種別の、削除されていない備品の数。ほかの人が確定させたばかりの行も数え、数えた行を押さえる（FOR SHARE）。
	 * K-04 で、使っている備品がないかを確かめるため（詳細設計 equipment/category/K-04_種別を消す.md の8章）。
	 */
	long countByCategoryIdForShare(@Param("categoryId") long categoryId);

	/**
	 * 削除されていない備品を1つ読み、行を押さえる（FOR UPDATE）。確かめてから書き換えるまでの間に、ほかの人に変えられないようにする
	 * （C-06 の8章）。ない・削除済みなら null。Example では FOR UPDATE を書けないので、手で書く。
	 */
	Equipment selectByIdForUpdate(@Param("id") long id);

	/**
	 * ID の一覧の、削除されていない備品を、まとめて読み、行を押さえる（FOR UPDATE）。結果は ID の昇順（主キーの順に押さえる）。
	 * 削除済み・ない ID は、結果に出ない。ids が空なら、何も返さない（B-04 の7章）。
	 */
	List<Equipment> selectByIdsForUpdate(@Param("ids") List<Long> ids);

	/**
	 * ID の一覧の、削除されていない備品の最終確認日を、まとめて変える。version は、列の値に1を足す（行ごとに version が違うため、式で書く。
	 * データ設計 2章：保存のたびに1増やす）。更新日時・更新者も入れる。更新した件数を返す。ids が空なら、何も更新しない（0）（B-04 の7章）。
	 */
	int updateLastConfirmedOnByIds(@Param("ids") List<Long> ids, @Param("lastConfirmedOn") LocalDate lastConfirmedOn,
			@Param("updatedAt") LocalDateTime updatedAt, @Param("updatedBy") long updatedBy);

	/**
	 * 条件に合う、削除されていない備品の数（C-01 の7章）。
	 * キーワードを小文字にそろえて複数の列で OR で探す、管理中の備品にだけ確認の条件を効かせる、などは Example では書けないので、手で書く。
	 */
	long countBySearch(EquipmentSearchCondition condition);

	/** 条件に合う、削除されていない備品を、並び順で limit 件まで読む（C-01 の7章）。 */
	List<Equipment> selectBySearch(EquipmentSearchCondition condition);
}
