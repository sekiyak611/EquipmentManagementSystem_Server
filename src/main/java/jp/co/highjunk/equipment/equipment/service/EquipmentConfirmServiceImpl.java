package jp.co.highjunk.equipment.equipment.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.dto.equipment.EquipmentListItem;
import jp.co.highjunk.equipment.equipment.model.Equipment;
import jp.co.highjunk.equipment.equipment.model.Loan;
import jp.co.highjunk.equipment.equipment.validator.EquipmentValidator;
import jp.co.highjunk.equipment.mapper.equipment.EquipmentMapper;
import jp.co.highjunk.equipment.mapper.equipment.LoanMapper;

/**
 * C-06 備品を「確認した」にするの業務のルールの中身（詳細設計 equipment/C-06_確認した.md の4・5章）。
 */
@Service
public class EquipmentConfirmServiceImpl implements EquipmentConfirmService {

	private static final Logger log = LoggerFactory.getLogger(EquipmentConfirmServiceImpl.class);

	private final EquipmentMapper equipmentMapper;

	private final LoanMapper loanMapper;

	private final EquipmentValidator equipmentValidator;

	private final EquipmentItemAssembler equipmentItemAssembler;

	private final Clock clock;

	public EquipmentConfirmServiceImpl(EquipmentMapper equipmentMapper, LoanMapper loanMapper,
			EquipmentValidator equipmentValidator, EquipmentItemAssembler equipmentItemAssembler, Clock clock) {
		this.equipmentMapper = equipmentMapper;
		this.loanMapper = loanMapper;
		this.equipmentValidator = equipmentValidator;
		this.equipmentItemAssembler = equipmentItemAssembler;
		this.clock = clock;
	}

	@Override
	@Transactional
	public EquipmentListItem confirm(long equipmentId, long userId) {
		// 備品の行を押さえてから確かめる（8章）。今月すでに確認済みでも誤りにせず、今日にする（5章の No.3）
		Equipment equipment = equipmentValidator.validateConfirmable(equipmentId);
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate today = now.toLocalDate();
		equipmentMapper.updateLastConfirmedOn(equipmentId, today, now, userId);
		equipment.setLastConfirmedOn(today);
		log.info("備品を確認しました（id={}, managementNo={}）", equipment.getId(), equipment.getManagementNo());

		Loan loan = loanMapper.selectActiveByEquipmentId(equipmentId);
		Map<Long, Long> borrower = loan == null ? Map.of() : Map.of(equipmentId, loan.getUserId());
		return equipmentItemAssembler.toItems(List.of(equipment), borrower).get(0);
	}
}
