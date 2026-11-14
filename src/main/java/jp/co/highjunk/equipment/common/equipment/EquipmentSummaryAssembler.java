package jp.co.highjunk.equipment.common.equipment;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.calendar.MonthlyConfirmationRule;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatus;
import jp.co.highjunk.equipment.dao.entity.Category;
import jp.co.highjunk.equipment.dao.entity.CategoryExample;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.entity.UserExample;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

/**
 * 備品の行から、一覧の1件の形（EquipmentSummary）を作る（詳細設計 common.md）。C-01・C-06 で使うので、common に置く
 * （CLAUDE.md 4.1。2026-11-15 に決めた）。
 * 種別の名前と借用者の氏名は、IN でまとめて1回で読み、Java で突き合わせる（JOIN を使わない。N+1 を防ぐ。CLAUDE.md 5章）。読むだけ。
 */
@Component
@RequiredArgsConstructor
public class EquipmentSummaryAssembler {

	private final CategoryMapper categoryMapper;

	private final UserMapper userMapper;

	private final MonthlyConfirmationRule monthlyConfirmationRule;

	/**
	 * @param rows                備品の行（並びはそのまま返す）。id・managementNo・categoryId・maker・model・status・lastConfirmedOn を使う
	 * @param borrowerByEquipment 貸出中の備品のID → 借用者のユーザID（ほかの備品は入っていてもよい）
	 */
	public List<EquipmentSummary> toSummaries(List<Equipment> rows, Map<Long, Long> borrowerByEquipment) {
		if (rows.isEmpty()) {
			return List.of();
		}
		Map<Long, String> categoryNames = categoryNames(
				rows.stream().map(Equipment::getCategoryId).collect(Collectors.toCollection(LinkedHashSet::new)));
		Set<Long> userIds = rows.stream().map(row -> borrowerByEquipment.get(row.getId()))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Map<Long, String> userNames = userNames(userIds);
		return rows.stream().map(row -> {
			Long borrowerId = borrowerByEquipment.get(row.getId());
			return new EquipmentSummary(row.getId(), row.getManagementNo(), categoryNames.get(row.getCategoryId()),
					row.getMaker(), row.getModel(), EquipmentStatus.valueOf(row.getStatus()),
					borrowerId == null ? LoanStatus.IN_STOCK : LoanStatus.LENT,
					borrowerId == null ? null : userNames.get(borrowerId), row.getLastConfirmedOn(),
					monthlyConfirmationRule.isConfirmedThisMonth(row.getLastConfirmedOn()));
		}).toList();
	}

	/** 種別の名前（備品が指していれば名前を出すため、削除済みの種別も読む。C-01 の7章） */
	private Map<Long, String> categoryNames(Set<Long> categoryIds) {
		CategoryExample example = new CategoryExample();
		example.createCriteria().andIdIn(List.copyOf(categoryIds));
		return categoryMapper.selectByExample(example).stream()
				.collect(Collectors.toMap(Category::getId, Category::getName));
	}

	/** 借用者の氏名（削除済みのユーザも読む。C-01 の7章） */
	private Map<Long, String> userNames(Set<Long> userIds) {
		if (userIds.isEmpty()) {
			return Map.of();
		}
		UserExample example = new UserExample();
		example.createCriteria().andIdIn(List.copyOf(userIds));
		return userMapper.selectByExample(example).stream().collect(Collectors.toMap(User::getId, User::getName));
	}
}
