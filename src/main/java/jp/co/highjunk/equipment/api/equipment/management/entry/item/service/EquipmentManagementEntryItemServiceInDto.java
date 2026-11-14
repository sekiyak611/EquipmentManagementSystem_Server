package jp.co.highjunk.equipment.api.equipment.management.entry.item.service;

import java.time.LocalDate;

import jp.co.highjunk.equipment.common.validation.CategoryExists;
import jp.co.highjunk.equipment.common.validation.EquipmentManagementNoNotDuplicated;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * C-03 で Service に渡す値と、DB との整合性のチェック（詳細設計 equipment/management/C-03_備品を登録する.md の2.5・6章）。
 * Controller が、RestInDto とログインしている人のユーザIDから作る（CLAUDE.md 5章）。
 * 管理番号の重なりと種別があるかは、同じ段（payload なし）で確かめるので、両方の誤りを入力欄ごとにまとめて返す（管理番号・種別の順）。
 */
@Getter
@AllArgsConstructor
public class EquipmentManagementEntryItemServiceInDto {

	/** 管理番号 */
	@EquipmentManagementNoNotDuplicated
	private final String managementNo;

	/** 種別のID。種別の行を FOR SHARE で押さえて読むので、確かめてから足すまでの間に K-04 で消されない（C-03 の8章） */
	@CategoryExists(forShare = true, message = "種別を選んでください")
	private final Long categoryId;

	/** メーカー */
	private final String maker;

	/** モデル */
	private final String model;

	/** シリアルNo */
	private final String serialNo;

	/** サイズ */
	private final String size;

	/** 解像度 */
	private final String resolution;

	/** 昇降 */
	private final Boolean heightAdjustable;

	/** ピボット */
	private final Boolean pivot;

	/** 購入日 */
	private final LocalDate purchasedOn;

	/** 保証期日 */
	private final LocalDate warrantyUntil;

	/** 備考 */
	private final String note;

	/** ログインしている人のユーザID（作った人・変えた人に入れる） */
	private final long loginUserId;
}
