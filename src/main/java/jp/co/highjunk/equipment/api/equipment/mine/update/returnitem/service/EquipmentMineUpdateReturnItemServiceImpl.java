package jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.model.EquipmentMineUpdateReturnItemRestOutDto;
import jp.co.highjunk.equipment.common.equipment.MyLoanListAssembler;
import jp.co.highjunk.equipment.common.validation.ServiceInDtoChecker;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.EquipmentExample;
import jp.co.highjunk.equipment.dao.entity.Loan;
import jp.co.highjunk.equipment.dao.entity.LoanExample;
import jp.co.highjunk.equipment.dao.mapper.EquipmentMapper;
import jp.co.highjunk.equipment.dao.mapper.LoanMapper;
import lombok.RequiredArgsConstructor;

/**
 * B-03 備品を返すの業務の処理の中身（詳細設計 equipment/mine/B-03_備品を返す.md の4・5・7・8章）。
 * 業務チェックは、受け取った貸出履歴のID だけで確かめられるので ServiceInDto で行い、Validator は作らない。
 * 行を押さえる順は、貸出履歴 → 備品（8章）。ServiceInDto のチェックが、この順に押さえる。
 */
@Service
@RequiredArgsConstructor
public class EquipmentMineUpdateReturnItemServiceImpl implements EquipmentMineUpdateReturnItemService {

	private static final Logger log = LoggerFactory.getLogger(EquipmentMineUpdateReturnItemServiceImpl.class);

	private final ServiceInDtoChecker serviceInDtoChecker;

	private final LoanMapper loanMapper;

	private final EquipmentMapper equipmentMapper;

	private final MyLoanListAssembler myLoanListAssembler;

	private final Clock clock;

	@Override
	@Transactional
	public EquipmentMineUpdateReturnItemRestOutDto returnItem(EquipmentMineUpdateReturnItemServiceInDto serviceInDto) {
		// 貸出履歴・備品の行を押さえてから確かめる（8章）。守れなければ、ここで例外になり、何も書き換えない
		serviceInDtoChecker.check(serviceInDto);

		long loanId = serviceInDto.getLoanId();
		long loginUserId = serviceInDto.getLoginUserId();
		LocalDateTime now = LocalDateTime.now(clock);
		// 返却日は、送られた値を使わず、サーバーの今日（機能一覧 5章の3）
		LocalDate today = now.toLocalDate();

		// 行を押さえたあとの最新の値を読む（version を +1 するため。ログの管理番号のため）
		Loan loan = loanMapper.selectByPrimaryKey(loanId);
		Equipment equipment = equipmentMapper.selectByPrimaryKey(loan.getEquipmentId());

		// 貸出履歴：返却日・メモ・version・更新日時・更新者。メモがなければ、今あるメモを変えない。
		// 返却日を入れると、active_equipment_id が自動で空になり、その備品をまた借りられる（B-03 の7章）
		Loan loanRow = new Loan();
		loanRow.setReturnedOn(today);
		loanRow.setMemo(serviceInDto.getMemo());
		loanRow.setVersion(loan.getVersion() + 1);
		loanRow.setUpdatedAt(now);
		loanRow.setUpdatedBy(loginUserId);
		LoanExample loanExample = new LoanExample();
		loanExample.createCriteria().andIdEqualTo(loanId).andDeletedAtIsNull();
		loanMapper.updateByExampleSelective(loanRow, loanExample);

		// 備品：最終確認日を今日にし、保存のたびに version を1増やす（機能一覧 5章の6。C-06 と同じ）
		Equipment equipmentRow = new Equipment();
		equipmentRow.setLastConfirmedOn(today);
		equipmentRow.setVersion(equipment.getVersion() + 1);
		equipmentRow.setUpdatedAt(now);
		equipmentRow.setUpdatedBy(loginUserId);
		EquipmentExample equipmentExample = new EquipmentExample();
		equipmentExample.createCriteria().andIdEqualTo(equipment.getId()).andDeletedAtIsNull();
		equipmentMapper.updateByExampleSelective(equipmentRow, equipmentExample);

		// メモの中身は、ログに出さない（API設計 7章）
		log.info("備品を返却しました（loanId={}, managementNo={}）", loanId, equipment.getManagementNo());

		return EquipmentMineUpdateReturnItemRestOutDto.from(myLoanListAssembler.assemble(loginUserId));
	}
}
