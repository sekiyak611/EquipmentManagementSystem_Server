package jp.co.highjunk.equipment.dto.equipment;

import java.util.List;
import java.util.Locale;

import jp.co.highjunk.equipment.common.constant.ConfirmFilter;
import jp.co.highjunk.equipment.common.constant.EquipmentSort;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatusFilter;
import jp.co.highjunk.equipment.common.error.ApiMessages;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;

/**
 * C-01 備品を探す条件（詳細設計 equipment/C-01_備品を探す.md の2.2・6章）。受け取った値を確かめて、この形にする。
 *
 * @param keyword    キーワード（前後の空白を取り除き、小文字にしたもの）。空なら null
 * @param categoryId 種別のID。null なら絞らない
 * @param statuses   管理状態。空なら絞らない
 * @param loanStatus 貸出状態の絞り込み
 * @param confirm    今月確認済みかの絞り込み
 * @param sort       並び順の列
 * @param descending 逆順なら true
 * @param page       何ページ目か（1以上）
 */
public record EquipmentListRequest(String keyword, Long categoryId, List<EquipmentStatus> statuses,
		LoanStatusFilter loanStatus, ConfirmFilter confirm, EquipmentSort sort, boolean descending, int page) {

	/**
	 * アドレスの後ろの値から作る。status・loanStatus・confirm・categoryId・page の型は Spring が確かめている（6章）。
	 *
	 * @throws BusinessRuleException sort が決まった値でない、page が 1 より小さいとき（400「入力内容に誤りがあります。」。9章の No.2）
	 */
	public static EquipmentListRequest of(String keyword, Long categoryId, List<EquipmentStatus> statuses,
			LoanStatusFilter loanStatus, ConfirmFilter confirm, String sort, int page) {
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
		return new EquipmentListRequest(trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT), categoryId,
				statuses == null ? List.of() : List.copyOf(statuses),
				loanStatus == null ? LoanStatusFilter.ALL : loanStatus, confirm == null ? ConfirmFilter.ALL : confirm,
				sortKey, descending, page);
	}
}
