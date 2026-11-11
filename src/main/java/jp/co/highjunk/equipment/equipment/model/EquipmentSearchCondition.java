package jp.co.highjunk.equipment.equipment.model;

import java.time.LocalDate;
import java.util.List;

import jp.co.highjunk.equipment.common.constant.ConfirmFilter;
import jp.co.highjunk.equipment.common.constant.EquipmentSort;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;

/**
 * 備品を探す条件（EquipmentMapper#countBySearch・selectBySearch に渡す。詳細設計 equipment/C-01_備品を探す.md 7章）。
 * テーブルの1行ではないが、mapper が使ってよいのは同じ機能の model と common だけなので、ここに置く（equipment.md 4章の No.1）。
 */
public class EquipmentSearchCondition {

	/** 種別のID。null なら絞らない */
	private Long categoryId;

	/** 管理状態。空なら絞らない */
	private List<EquipmentStatus> statuses;

	/** 貸出中だけにするときの、貸出中の備品のID。null なら絞らない（空のときは呼ぶ側で0件にする） */
	private List<Long> lentEquipmentIds;

	/** 保管中だけにするときの、除く備品のID（貸出中の備品）。null・空なら絞らない */
	private List<Long> notLentEquipmentIds;

	/** 今月確認済みかの絞り込み（管理中の備品にだけ効かせる） */
	private ConfirmFilter confirm;

	/** 今月の1日（MonthlyConfirmationRule。common.md 3.11） */
	private LocalDate firstDayOfThisMonth;

	/** キーワードの LIKE の形（%小文字のキーワード%。% _ ! には ! を付けたもの）。null なら絞らない */
	private String keywordPattern;

	/** キーワードを氏名に含む借用者が借りている備品のID。空なら、この条件は使わない */
	private List<Long> keywordEquipmentIds;

	/** 並び順の列 */
	private EquipmentSort sort;

	/** 逆順なら true */
	private boolean descending;

	/** 読む件数 */
	private int limit;

	/** 飛ばす件数 */
	private int offset;

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public List<EquipmentStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<EquipmentStatus> statuses) {
		this.statuses = statuses;
	}

	public List<Long> getLentEquipmentIds() {
		return lentEquipmentIds;
	}

	public void setLentEquipmentIds(List<Long> lentEquipmentIds) {
		this.lentEquipmentIds = lentEquipmentIds;
	}

	public List<Long> getNotLentEquipmentIds() {
		return notLentEquipmentIds;
	}

	public void setNotLentEquipmentIds(List<Long> notLentEquipmentIds) {
		this.notLentEquipmentIds = notLentEquipmentIds;
	}

	public ConfirmFilter getConfirm() {
		return confirm;
	}

	public void setConfirm(ConfirmFilter confirm) {
		this.confirm = confirm;
	}

	public LocalDate getFirstDayOfThisMonth() {
		return firstDayOfThisMonth;
	}

	public void setFirstDayOfThisMonth(LocalDate firstDayOfThisMonth) {
		this.firstDayOfThisMonth = firstDayOfThisMonth;
	}

	public String getKeywordPattern() {
		return keywordPattern;
	}

	public void setKeywordPattern(String keywordPattern) {
		this.keywordPattern = keywordPattern;
	}

	public List<Long> getKeywordEquipmentIds() {
		return keywordEquipmentIds;
	}

	public void setKeywordEquipmentIds(List<Long> keywordEquipmentIds) {
		this.keywordEquipmentIds = keywordEquipmentIds;
	}

	public EquipmentSort getSort() {
		return sort;
	}

	public void setSort(EquipmentSort sort) {
		this.sort = sort;
	}

	public boolean isDescending() {
		return descending;
	}

	public void setDescending(boolean descending) {
		this.descending = descending;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
