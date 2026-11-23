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
import jp.co.highjunk.equipment.dao.entity.LoanExample;
import jp.co.highjunk.equipment.dao.mapper.LoanMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.EquipmentCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * B-04 自分が借りている備品を「確認した」にするの業務の処理の中身（詳細設計 equipment/mine/B-04_確認した.md の4・5・7・8章）。
 * 備品の行だけを、まとめて押さえて読み（FOR UPDATE。備品の ID の昇順）、そのあとで、今借りている貸出履歴を、押さえずに読み、
 * Validator で確かめてから、備品をまとめて更新する。貸出履歴は、押さえず、書き換えない（8章。障害 ZPOP-434）。
 */
@Service
@RequiredArgsConstructor
public class EquipmentMineUpdateItemServiceImpl implements EquipmentMineUpdateItemService {

	private static final Logger log = LoggerFactory.getLogger(EquipmentMineUpdateItemServiceImpl.class);

	private final LoanMapper loanMapper;

	private final EquipmentCustomMapper equipmentCustomMapper;

	private final EquipmentMineUpdateItemValidator validator;

	private final MyLoanListAssembler myLoanListAssembler;

	private final Clock clock;

	@Override
	@Transactional
	public EquipmentMineUpdateItemRestOutDto confirm(EquipmentMineUpdateItemServiceInDto serviceInDto) {
		long loginUserId = serviceInDto.getLoginUserId();
		// 送られた ID の重なりを、1つにまとめる（12章の No.5）。押さえる順は、SQL（主キーの順）が決める（8章）
		List<Long> equipmentIds = serviceInDto.getEquipmentIds().stream().distinct().toList();

		// 備品の行だけを、先に押さえて読む（8章。障害 ZPOP-434：貸出履歴を押さえると、B-03 と行き違って、デッドロックになる）。
		// これが、このまとまりで最初の読みで、普通の SELECT を、これより前に挟まない
		List<Equipment> equipments = equipmentCustomMapper.selectByIdsForUpdate(equipmentIds);
		// 備品を押さえたあとで、今借りている貸出履歴を、押さえずに（普通の SELECT で）読む。そのとき確定している記録を見る
		LoanExample loanExample = new LoanExample();
		loanExample.createCriteria().andEquipmentIdIn(equipmentIds).andReturnedOnIsNull().andDeletedAtIsNull();
		List<Loan> currentLoans = loanMapper.selectByExample(loanExample);

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
