package jp.co.highjunk.equipment.api.equipment.management.search.item.controller;

import java.util.List;
import java.util.Locale;

import jp.co.highjunk.equipment.common.constant.ConfirmFilter;
import jp.co.highjunk.equipment.common.constant.EquipmentSort;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatusFilter;
import jp.co.highjunk.equipment.common.error.ApiMessages;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * C-01 で画面から受け取る値（アドレスの後ろの値）と、形式のチェック（詳細設計 equipment/management/C-01_備品を探す.md の2.2・6章）。
 * アドレスの後ろの値は、決まった値でないときの返し方（errors なしの 400）を変えないため、@Valid ではなく of で確かめる。
 */
@Getter
@AllArgsConstructor
public class EquipmentManagementSearchItemRestInDto {

	/** キーワード（前後の空白を取り除き、小文字にしたもの）。空なら null */
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

	/**
	 * アドレスの後ろの値から作る。status・loanStatus・confirm・categoryId・page の型は Spring が確かめている（6章）。
	 *
	 * @throws BusinessRuleException sort が決まった値でない、page が 1 より小さいとき（400「入力内容に誤りがあります。」。9章の No.2）
	 */
	public static EquipmentManagementSearchItemRestInDto of(String keyword, Long categoryId,
			List<EquipmentStatus> statuses, LoanStatusFilter loanStatus, ConfirmFilter confirm, String sort, int page) {
		if (page < 1) {
			throw new BusinessRuleException(ApiMessages.VALIDATION_ERROR);
		}
		EquipmentSort sortKey;
		boolean descending;
		switch (sort == null ? "managementNo" : sort) {
		case "managementNo" -> {
			sortKey = EquipmentSort.MANAGEMENT_NO;
			descending = false;
		}
		case "managementNo,desc" -> {
			sortKey = EquipmentSort.MANAGEMENT_NO;
			descending = true;
		}
		case "lastConfirmedOn" -> {
			sortKey = EquipmentSort.LAST_CONFIRMED_ON;
			descending = false;
		}
		case "lastConfirmedOn,desc" -> {
			sortKey = EquipmentSort.LAST_CONFIRMED_ON;
			descending = true;
		}
		default -> throw new BusinessRuleException(ApiMessages.VALIDATION_ERROR);
		}
		// 大文字・小文字を区別せずに探すため、小文字にそろえる（データ設計 2章）
		String trimmed = keyword == null ? "" : keyword.strip();
		return new EquipmentManagementSearchItemRestInDto(trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT),
				categoryId, statuses == null ? List.of() : List.copyOf(statuses),
				loanStatus == null ? LoanStatusFilter.ALL : loanStatus, confirm == null ? ConfirmFilter.ALL : confirm,
				sortKey, descending, page);
	}
}
