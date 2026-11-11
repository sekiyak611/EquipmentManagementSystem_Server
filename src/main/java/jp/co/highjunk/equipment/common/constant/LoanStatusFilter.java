package jp.co.highjunk.equipment.common.constant;

/**
 * 貸出状態の絞り込み（C-01 の条件 loanStatus。API設計 4.8）。
 */
public enum LoanStatusFilter {

	/** すべて */
	ALL,

	/** 保管中だけ */
	IN_STOCK,

	/** 貸出中だけ */
	LENT
}
