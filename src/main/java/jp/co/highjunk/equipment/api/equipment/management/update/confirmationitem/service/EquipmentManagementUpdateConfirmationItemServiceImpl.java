package jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.api.equipment.management.update.confirmationitem.model.EquipmentManagementUpdateConfirmationItemRestOutDto;
import jp.co.highjunk.equipment.common.equipment.EquipmentSummaryAssembler;
import jp.co.highjunk.equipment.common.validation.ServiceInDtoChecker;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.EquipmentExample;
import jp.co.highjunk.equipment.dao.entity.Loan;
import jp.co.highjunk.equipment.dao.entity.LoanExample;
import jp.co.highjunk.equipment.dao.mapper.EquipmentMapper;
import jp.co.highjunk.equipment.dao.mapper.LoanMapper;
import lombok.RequiredArgsConstructor;

/**
 * C-06 備品を「確認した」にするの業務の処理の中身（詳細設計 equipment/management/C-06_確認した.md の4・5章）。
 * 業務チェックは、受け取った備品のID だけで確かめられるので ServiceInDto で行い、Validator は作らない。
 */
@Service
@RequiredArgsConstructor
public class EquipmentManagementUpdateConfirmationItemServiceImpl
		implements EquipmentManagementUpdateConfirmationItemService {

	private static final Logger log = LoggerFactory
			.getLogger(EquipmentManagementUpdateConfirmationItemServiceImpl.class);

	private final ServiceInDtoChecker serviceInDtoChecker;

	private final EquipmentMapper equipmentMapper;

	private final LoanMapper loanMapper;

	private final EquipmentSummaryAssembler equipmentSummaryAssembler;

	private final Clock clock;

	@Override
	@Transactional
	public EquipmentManagementUpdateConfirmationItemRestOutDto confirm(
			EquipmentManagementUpdateConfirmationItemServiceInDto serviceInDto) {
		// 備品の行を押さえてから確かめる（8章）。今月すでに確認済みでも誤りにせず、今日にする（5章の No.3）
		serviceInDtoChecker.check(serviceInDto);

		long id = serviceInDto.getId();
		Equipment equipment = equipmentMapper.selectByPrimaryKey(id);
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate today = now.toLocalDate();

		// 最終確認日だけを変える。保存のたびに version を1増やす（データ設計 2章。2026-11-11 に人が決めた）
		Equipment row = new Equipment();
		row.setLastConfirmedOn(today);
		row.setVersion(equipment.getVersion() + 1);
		row.setUpdatedAt(now);
		row.setUpdatedBy(serviceInDto.getLoginUserId());
		EquipmentExample example = new EquipmentExample();
		example.createCriteria().andIdEqualTo(id).andDeletedAtIsNull();
		equipmentMapper.updateByExampleSelective(row, example);
		equipment.setLastConfirmedOn(today);
		log.info("備品を確認しました（id={}, managementNo={}）", equipment.getId(), equipment.getManagementNo());

		// 貸出中の貸出履歴（二重貸出は重複禁止で防いでいるので、0〜1件）
		LoanExample loanExample = new LoanExample();
		loanExample.createCriteria().andEquipmentIdEqualTo(id).andReturnedOnIsNull().andDeletedAtIsNull();
		List<Loan> loans = loanMapper.selectByExample(loanExample);
		Map<Long, Long> borrower = loans.isEmpty() ? Map.of() : Map.of(id, loans.get(0).getUserId());
		return EquipmentManagementUpdateConfirmationItemRestOutDto
				.from(equipmentSummaryAssembler.toSummaries(List.of(equipment), borrower).get(0));
	}
}
