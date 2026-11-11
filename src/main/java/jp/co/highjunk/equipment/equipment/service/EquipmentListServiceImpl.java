package jp.co.highjunk.equipment.equipment.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.common.calendar.MonthlyConfirmationRule;
import jp.co.highjunk.equipment.common.constant.LoanStatusFilter;
import jp.co.highjunk.equipment.dto.equipment.EquipmentListRequest;
import jp.co.highjunk.equipment.dto.equipment.EquipmentListResponse;
import jp.co.highjunk.equipment.equipment.model.Equipment;
import jp.co.highjunk.equipment.equipment.model.EquipmentSearchCondition;
import jp.co.highjunk.equipment.equipment.model.Loan;
import jp.co.highjunk.equipment.mapper.equipment.EquipmentMapper;
import jp.co.highjunk.equipment.mapper.equipment.LoanMapper;
import jp.co.highjunk.equipment.mapper.equipment.UserMapper;

/**
 * C-01 備品を探すの業務のルールの中身（詳細設計 equipment/C-01_備品を探す.md の4・5・7章）。
 * JOIN とサブクエリを使わず、貸出中の貸出履歴と、氏名で探したユーザを先に読んで備品のIDの一覧にし、備品の SQL の IN に渡す。
 * 種別の名前と借用者の氏名は、ページの備品を読んだあとで EquipmentItemAssembler が IN でまとめて読み、Java で突き合わせる（CLAUDE.md 5章）。
 * 読むだけなので @Transactional は付けない（8章）。
 */
@Service
public class EquipmentListServiceImpl implements EquipmentListService {

	/** 1ページの件数（画面設計 10章、API設計 2.5） */
	static final int PAGE_SIZE = 50;

	private final EquipmentMapper equipmentMapper;

	private final LoanMapper loanMapper;

	private final UserMapper userMapper;

	private final MonthlyConfirmationRule monthlyConfirmationRule;

	private final EquipmentItemAssembler equipmentItemAssembler;

	public EquipmentListServiceImpl(EquipmentMapper equipmentMapper, LoanMapper loanMapper, UserMapper userMapper,
			MonthlyConfirmationRule monthlyConfirmationRule, EquipmentItemAssembler equipmentItemAssembler) {
		this.equipmentMapper = equipmentMapper;
		this.loanMapper = loanMapper;
		this.userMapper = userMapper;
		this.monthlyConfirmationRule = monthlyConfirmationRule;
		this.equipmentItemAssembler = equipmentItemAssembler;
	}

	@Override
	public EquipmentListResponse list(EquipmentListRequest request) {
		// 貸出中の貸出履歴：備品のID → 借用者のユーザID（5章の No.2）
		Map<Long, Long> borrowerByEquipment = new HashMap<>();
		for (Loan loan : loanMapper.selectActive()) {
			borrowerByEquipment.put(loan.getEquipmentId(), loan.getUserId());
		}

		EquipmentSearchCondition condition = new EquipmentSearchCondition();
		condition.setCategoryId(request.categoryId());
		condition.setStatuses(request.statuses());
		condition.setConfirm(request.confirm());
		condition.setFirstDayOfThisMonth(monthlyConfirmationRule.firstDayOfThisMonth());
		condition.setSort(request.sort());
		condition.setDescending(request.descending());
		condition.setLimit(PAGE_SIZE);
		condition.setOffset((request.page() - 1) * PAGE_SIZE);

		List<Long> lentIds = List.copyOf(borrowerByEquipment.keySet());
		if (request.loanStatus() == LoanStatusFilter.LENT) {
			if (lentIds.isEmpty()) {
				// 貸出中の備品がなければ、SQL を出さずに0件（7章の探す条件）
				return new EquipmentListResponse(List.of(), request.page(), PAGE_SIZE, 0, 0);
			}
			condition.setLentEquipmentIds(lentIds);
		} else if (request.loanStatus() == LoanStatusFilter.IN_STOCK) {
			condition.setNotLentEquipmentIds(lentIds);
		}

		if (request.keyword() != null) {
			String pattern = "%" + escapeLike(request.keyword()) + "%";
			condition.setKeywordPattern(pattern);
			// 借用者の氏名で探す：氏名にキーワードを含むユーザが借りている備品のID
			Set<Long> userIds = Set.copyOf(userMapper.selectIdsByNameContaining(pattern));
			condition.setKeywordEquipmentIds(borrowerByEquipment.entrySet().stream()
					.filter(entry -> userIds.contains(entry.getValue()))
					.map(Map.Entry::getKey)
					.toList());
		}

		long totalCount = equipmentMapper.countBySearch(condition);
		int totalPages = (int) ((totalCount + PAGE_SIZE - 1) / PAGE_SIZE);
		List<Equipment> rows = totalCount == 0 ? List.of() : equipmentMapper.selectBySearch(condition);
		return new EquipmentListResponse(equipmentItemAssembler.toItems(rows, borrowerByEquipment), request.page(), PAGE_SIZE, totalCount,
				totalPages);
	}

	/** LIKE の特別な文字（% と _）と、印にする ! の前に ! を付けて、ふつうの文字として探す（C-01 の12章 No.6） */
	static String escapeLike(String keyword) {
		return keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_");
	}
}
