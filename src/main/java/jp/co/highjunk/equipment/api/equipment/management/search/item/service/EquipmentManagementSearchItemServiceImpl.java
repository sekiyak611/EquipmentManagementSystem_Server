package jp.co.highjunk.equipment.api.equipment.management.search.item.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.api.equipment.management.search.item.model.EquipmentManagementSearchItemRestOutDto;
import jp.co.highjunk.equipment.common.calendar.MonthlyConfirmationRule;
import jp.co.highjunk.equipment.common.constant.LoanStatusFilter;
import jp.co.highjunk.equipment.common.equipment.EquipmentSummaryAssembler;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.Loan;
import jp.co.highjunk.equipment.dao.entity.LoanExample;
import jp.co.highjunk.equipment.dao.entity.custom.EquipmentSearchCondition;
import jp.co.highjunk.equipment.dao.mapper.LoanMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.EquipmentCustomMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.UserCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * C-01 備品を探すの業務の処理の中身（詳細設計 equipment/management/C-01_備品を探す.md の4・5・7章）。
 * JOIN とサブクエリを使わず、貸出中の貸出履歴と、氏名で探したユーザを先に読んで備品のIDの一覧にし、備品の SQL の IN に渡す。
 * 種別の名前と借用者の氏名は、ページの備品を読んだあとで EquipmentSummaryAssembler が IN でまとめて読み、Java で突き合わせる（CLAUDE.md 5章）。
 * 読むだけなので @Transactional は付けない（8章）。
 */
@Service
@RequiredArgsConstructor
public class EquipmentManagementSearchItemServiceImpl implements EquipmentManagementSearchItemService {

	/** 1ページの件数（画面設計 10章、API設計 2.5） */
	static final int PAGE_SIZE = 50;

	private final EquipmentCustomMapper equipmentCustomMapper;

	private final LoanMapper loanMapper;

	private final UserCustomMapper userCustomMapper;

	private final MonthlyConfirmationRule monthlyConfirmationRule;

	private final EquipmentSummaryAssembler equipmentSummaryAssembler;

	@Override
	public EquipmentManagementSearchItemRestOutDto search(EquipmentManagementSearchItemServiceInDto serviceInDto) {
		// 貸出中の貸出履歴：備品のID → 借用者のユーザID（5章の No.2）。貸出中＝返却日が空で、取り消されていない行（データ設計 5.7）
		LoanExample loanExample = new LoanExample();
		loanExample.createCriteria().andReturnedOnIsNull().andDeletedAtIsNull();
		Map<Long, Long> borrowerByEquipment = new HashMap<>();
		for (Loan loan : loanMapper.selectByExample(loanExample)) {
			borrowerByEquipment.put(loan.getEquipmentId(), loan.getUserId());
		}
		EquipmentSearchCondition condition = new EquipmentSearchCondition();
		condition.setCategoryId(serviceInDto.getCategoryId());
		condition.setStatuses(serviceInDto.getStatuses());
		condition.setConfirm(serviceInDto.getConfirm());
		condition.setFirstDayOfThisMonth(monthlyConfirmationRule.firstDayOfThisMonth());
		condition.setSort(serviceInDto.getSort());
		condition.setDescending(serviceInDto.isDescending());
		condition.setLimit(PAGE_SIZE);
		condition.setOffset((serviceInDto.getPage() - 1) * PAGE_SIZE);
		List<Long> lentIds = List.copyOf(borrowerByEquipment.keySet());
		if (serviceInDto.getLoanStatus() == LoanStatusFilter.LENT) {
			if (lentIds.isEmpty()) {
				// 貸出中の備品がなければ、SQL を出さずに0件（7章の探す条件）
				return new EquipmentManagementSearchItemRestOutDto(List.of(), serviceInDto.getPage(), PAGE_SIZE, 0, 0);
			}
			condition.setLentEquipmentIds(lentIds);
		} else if (serviceInDto.getLoanStatus() == LoanStatusFilter.IN_STOCK) {
			condition.setNotLentEquipmentIds(lentIds);
		}
		if (serviceInDto.getKeyword() != null) {
			String pattern = "%" + escapeLike(serviceInDto.getKeyword()) + "%";
			condition.setKeywordPattern(pattern);
			// 借用者の氏名で探す：氏名にキーワードを含むユーザが借りている備品のID
			Set<Long> userIds = Set.copyOf(userCustomMapper.selectIdsByNameContaining(pattern));
			condition.setKeywordEquipmentIds(borrowerByEquipment.entrySet().stream()
					.filter(entry -> userIds.contains(entry.getValue()))
					.map(Map.Entry::getKey)
					.toList());
		}
		long totalCount = equipmentCustomMapper.countBySearch(condition);
		int totalPages = (int) ((totalCount + PAGE_SIZE - 1) / PAGE_SIZE);
		List<Equipment> rows = totalCount == 0 ? List.of() : equipmentCustomMapper.selectBySearch(condition);
		List<EquipmentManagementSearchItemRestOutDto.Item> items = equipmentSummaryAssembler
				.toSummaries(rows, borrowerByEquipment).stream()
				.map(EquipmentManagementSearchItemRestOutDto.Item::from)
				.toList();
		return new EquipmentManagementSearchItemRestOutDto(items, serviceInDto.getPage(), PAGE_SIZE, totalCount,
				totalPages);
	}

	/** LIKE の特別な文字（% と _）と、印にする ! の前に ! を付けて、ふつうの文字として探す（C-01 の12章 No.6） */
	static String escapeLike(String keyword) {
		return keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_");
	}
}
