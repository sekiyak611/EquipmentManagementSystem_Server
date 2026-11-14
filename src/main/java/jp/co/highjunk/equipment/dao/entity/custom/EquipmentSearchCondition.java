package jp.co.highjunk.equipment.dao.entity.custom;

import java.time.LocalDate;
import java.util.List;

import jp.co.highjunk.equipment.common.constant.ConfirmFilter;
import jp.co.highjunk.equipment.common.constant.EquipmentSort;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 備品を探す条件（EquipmentCustomMapper#countBySearch・selectBySearch に渡す。詳細設計 equipment/management/C-01_備品を探す.md 7章）。
 * テーブルの1行ではないので、手で書く Mapper の形として dao.entity.custom に置く（CLAUDE.md 4.1）。
 */
@Getter
@Setter
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
}
