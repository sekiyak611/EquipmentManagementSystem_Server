package jp.co.highjunk.equipment.api.equipment.management.search.item.service;

import java.util.List;

import jp.co.highjunk.equipment.common.constant.ConfirmFilter;
import jp.co.highjunk.equipment.common.constant.EquipmentSort;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatusFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * C-01 で Service に渡す値（詳細設計 equipment/management/C-01_備品を探す.md の2.5）。Controller が RestInDto から作る。
 * DB と照らして確かめることはない（ない種別で絞っても、0件になるだけ）。
 */
@Getter
@AllArgsConstructor
public class EquipmentManagementSearchItemServiceInDto {

	/** キーワード（小文字）。null なら絞らない */
	private final String keyword;

	/** 種別のID。null なら絞らない */
	private final Long categoryId;

	/** 管理状態。空なら絞らない */
	private final List<EquipmentStatus> statuses;

	/** 貸出状態の絞り込み */
	private final LoanStatusFilter loanStatus;

	/** 今月確認済みかの絞り込み */
	private final ConfirmFilter confirm;

	/** 並び順の列 */
	private final EquipmentSort sort;

	/** 逆順なら true */
	private final boolean descending;

	/** 何ページ目か（1以上） */
	private final int page;
}
