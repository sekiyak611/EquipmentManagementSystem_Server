package jp.co.highjunk.equipment.common.equipment;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.calendar.ReminderCalendar;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.EquipmentExample;
import jp.co.highjunk.equipment.dao.entity.Loan;
import jp.co.highjunk.equipment.dao.entity.LoanExample;
import jp.co.highjunk.equipment.dao.mapper.EquipmentMapper;
import jp.co.highjunk.equipment.dao.mapper.LoanMapper;
import lombok.RequiredArgsConstructor;

/**
 * ログインしている人が今借りている備品の一覧と、今月の確認の案内を作る（詳細設計 equipment/mine/B-01_自分の備品の一覧.md、common.md 3.19）。
 * B-01・B-04（確認したあとの最新の一覧）で使うので、common に置く（CLAUDE.md 4.1・5章）。読むだけ。
 * 貸出履歴・備品・種別・借用者は、テーブルごとに IN でまとめて読み、Java で突き合わせる（JOIN を使わない。N+1 を防ぐ）。
 */
@Component
@RequiredArgsConstructor
public class MyLoanListAssembler {

	private final LoanMapper loanMapper;

	private final EquipmentMapper equipmentMapper;

	private final EquipmentSummaryAssembler equipmentSummaryAssembler;

	private final ReminderCalendar reminderCalendar;

	private final Clock clock;

	/**
	 * @param userId 借りている人（ログインしている人）のユーザID
	 */
	public MyLoanList assemble(long userId) {
		List<MyLoanItem> items = items(userId);
		int unconfirmedCount = (int) items.stream().filter(item -> !item.isConfirmedThisMonth()).count();
		return new MyLoanList(unconfirmedCount, reminderCalendar.reminderAtOfThisMonth().toOffsetDateTime(),
				reminderCalendar.isReminderPassed(), items);
	}

	private List<MyLoanItem> items(long userId) {
		// 今借りている貸出履歴（返却されていない。取り消されていない）
		LoanExample loanExample = new LoanExample();
		loanExample.createCriteria().andUserIdEqualTo(userId).andReturnedOnIsNull().andDeletedAtIsNull();
		List<Loan> loans = loanMapper.selectByExample(loanExample);
		if (loans.isEmpty()) {
			return List.of();
		}

		// 管理中で、削除されていない備品だけ（Slack催促の対象・確認できる備品と同じ。詳細設計 B-01 の12章の No.2）
		EquipmentExample equipmentExample = new EquipmentExample();
		equipmentExample.createCriteria().andIdIn(loans.stream().map(Loan::getEquipmentId).toList())
				.andStatusEqualTo(EquipmentStatus.MANAGED.name()).andDeletedAtIsNull();
		List<Equipment> equipmentRows = equipmentMapper.selectByExample(equipmentExample);
		Map<Long, Loan> loanByEquipment = loans.stream()
				.collect(Collectors.toMap(Loan::getEquipmentId, Function.identity()));

		// 種別の名前・今月確認済みかは、備品の一覧の1件（C-01）を作る部品で求める
		Map<Long, Long> borrowerByEquipment = equipmentRows.stream()
				.collect(Collectors.toMap(Equipment::getId, row -> userId));
		Map<Long, EquipmentSummary> summaryByEquipment = equipmentSummaryAssembler
				.toSummaries(equipmentRows, borrowerByEquipment).stream()
				.collect(Collectors.toMap(EquipmentSummary::getId, Function.identity()));

		LocalDate today = LocalDate.now(clock);
		return equipmentRows.stream().map(row -> {
			Loan loan = loanByEquipment.get(row.getId());
			EquipmentSummary summary = summaryByEquipment.get(row.getId());
			boolean overdue = loan.getDueOn() != null && loan.getDueOn().isBefore(today);
			return new MyLoanItem(loan.getId(), row.getId(), summary.getManagementNo(), summary.getCategoryName(),
					summary.getMaker(), summary.getModel(), loan.getLentOn(), loan.getDueOn(), overdue,
					summary.getLastConfirmedOn(), summary.isConfirmedThisMonth());
		}).sorted(Comparator.comparing(MyLoanItem::isConfirmedThisMonth) // false（未確認）が先
				.thenComparing(MyLoanItem::getLentOn).thenComparing(MyLoanItem::getLoanId)).toList();
	}
}
