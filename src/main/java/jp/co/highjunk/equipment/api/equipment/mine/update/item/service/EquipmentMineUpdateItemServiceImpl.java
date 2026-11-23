package jp.co.highjunk.equipment.api.equipment.mine.update.item.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.api.equipment.mine.update.item.model.EquipmentMineUpdateItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.mine.update.item.validator.EquipmentMineUpdateItemValidator;
import jp.co.highjunk.equipment.api.equipment.mine.update.item.validator.EquipmentMineUpdateItemValidatorDto;
import jp.co.highjunk.equipment.common.equipment.MyLoanListAssembler;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.Loan;
import jp.co.highjunk.equipment.dao.mapper.custom.EquipmentCustomMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.LoanCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * B-04 自分が借りている備品を「確認した」にするの業務の処理の中身（詳細設計 equipment/mine/B-04_確認した.md の4・5・7・8章）。
 * 貸出履歴 → 備品の順に、まとめて行を押さえて読み（FOR UPDATE）、Validator で確かめてから、備品をまとめて更新する。
 * 貸出履歴は、押さえるだけで、書き換えない。普通の SELECT を先に挟まない（8章の「読み方」）。
 */
@Service
@RequiredArgsConstructor
public class EquipmentMineUpdateItemServiceImpl implements EquipmentMineUpdateItemService {

	private static final Logger log = LoggerFactory.getLogger(EquipmentMineUpdateItemServiceImpl.class);

	private final LoanCustomMapper loanCustomMapper;

	private final EquipmentCustomMapper equipmentCustomMapper;

	private final EquipmentMineUpdateItemValidator validator;

	private final MyLoanListAssembler myLoanListAssembler;

	private final Clock clock;

	@Override
	@Transactional
	public EquipmentMineUpdateItemRestOutDto confirm(EquipmentMineUpdateItemServiceInDto serviceInDto) {
		long loginUserId = serviceInDto.getLoginUserId();
		// 送られた ID の重なりを、1つにまとめる（12章の No.5）。押さえる順は、SQL（索引の順）が決める（8章）
		List<Long> equipmentIds = serviceInDto.getEquipmentIds().stream().distinct().toList();

		// 貸出履歴 → 備品の順に、行を押さえて読む（8章）
		List<Loan> currentLoans = loanCustomMapper.selectCurrentByEquipmentIdsForUpdate(equipmentIds);
		List<Equipment> equipments = equipmentCustomMapper.selectByIdsForUpdate(equipmentIds);

		// 守れなければ、ここで例外になり、何も書き換えない（404 → 403 → 400）
		validator.check(new EquipmentMineUpdateItemValidatorDto(loginUserId, equipmentIds, currentLoans, equipments));

		// 最終確認日を今日にする（日付は選べない）。保存のたびに version を1増やす（C-06 と同じ）。
		// 今月すでに確認済みの備品も、誤りにせず、今日にする（12章の No.4）。貸出履歴は変えない
		LocalDateTime now = LocalDateTime.now(clock);
		equipmentCustomMapper.updateLastConfirmedOnByIds(equipmentIds, now.toLocalDate(), now, loginUserId);

		log.info("備品を確認しました（managementNo={}）", equipments.stream().map(Equipment::getManagementNo).toList());

		// 更新のあとに呼ぶ（8章の「読み方」）
		return EquipmentMineUpdateItemRestOutDto.from(myLoanListAssembler.assemble(loginUserId));
	}
}
