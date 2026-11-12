package jp.co.highjunk.equipment.equipment.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.calendar.MonthlyConfirmationRule;
import jp.co.highjunk.equipment.common.constant.LoanStatus;
import jp.co.highjunk.equipment.dto.equipment.EquipmentListItem;
import jp.co.highjunk.equipment.equipment.model.Category;
import jp.co.highjunk.equipment.equipment.model.Equipment;
import jp.co.highjunk.equipment.equipment.model.User;
import jp.co.highjunk.equipment.mapper.equipment.CategoryMapper;
import jp.co.highjunk.equipment.mapper.equipment.UserMapper;

/**
 * 備品の行から、C-01 の1件の形（EquipmentListItem）を作る（詳細設計 equipment/equipment.md 2.1）。C-01・C-06 で使う。
 * 種別の名前と借用者の氏名は、IN でまとめて1回で読み、Java で突き合わせる（JOIN を使わない。N+1 を防ぐ。CLAUDE.md 5章）。
 * 読むだけ。API の service ではないので、役目を表す名前にした（CLAUDE.md 5章）。
 */
@Component
public class EquipmentItemAssembler {

	private final CategoryMapper categoryMapper;

	private final UserMapper userMapper;

	private final MonthlyConfirmationRule monthlyConfirmationRule;

	public EquipmentItemAssembler(CategoryMapper categoryMapper, UserMapper userMapper,
			MonthlyConfirmationRule monthlyConfirmationRule) {
		this.categoryMapper = categoryMapper;
		this.userMapper = userMapper;
		this.monthlyConfirmationRule = monthlyConfirmationRule;
	}

	/**
	 * @param rows                備品の行（並びはそのまま返す）
	 * @param borrowerByEquipment 貸出中の備品のID → 借用者のユーザID（ほかの備品は入っていてもよい）
	 */
	public List<EquipmentListItem> toItems(List<Equipment> rows, Map<Long, Long> borrowerByEquipment) {
		if (rows.isEmpty()) {
			return List.of();
		}
		Set<Long> categoryIds = rows.stream().map(Equipment::getCategoryId)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Map<Long, String> categoryNames = categoryMapper.selectByIds(categoryIds).stream()
				.collect(Collectors.toMap(Category::getId, Category::getName));

		Set<Long> userIds = rows.stream().map(row -> borrowerByEquipment.get(row.getId()))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Map<Long, String> userNames = userIds.isEmpty() ? Map.of()
				: userMapper.selectByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getName));

		return rows.stream().map(row -> {
			Long borrowerId = borrowerByEquipment.get(row.getId());
			return new EquipmentListItem(row.getId(), row.getManagementNo(), categoryNames.get(row.getCategoryId()),
					row.getMaker(), row.getModel(), row.getStatus(),
					borrowerId == null ? LoanStatus.IN_STOCK : LoanStatus.LENT,
					borrowerId == null ? null : userNames.get(borrowerId), row.getLastConfirmedOn(),
					monthlyConfirmationRule.isConfirmedThisMonth(row.getLastConfirmedOn()));
		}).toList();
	}
}
