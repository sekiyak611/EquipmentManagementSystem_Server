package jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.service;

import jp.co.highjunk.equipment.common.validation.LoanBorrowedBySelf;
import jp.co.highjunk.equipment.common.validation.LoanEquipmentManaged;
import jp.co.highjunk.equipment.common.validation.LoanExists;
import jp.co.highjunk.equipment.common.validation.LoanNotReturned;
import jp.co.highjunk.equipment.common.validation.ServiceCheckOrder;
import jp.co.highjunk.equipment.common.validation.ServiceErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * B-03 で Service に渡す値と、DB との整合性のチェック（詳細設計 equipment/mine/B-03_備品を返す.md の2.5・6章）。
 * Controller が、パスの貸出履歴のID・RestInDto・ログインしている人のユーザIDから作る（CLAUDE.md 5章）。
 * 確かめる順番：貸出履歴がある（404）→ 次の3つをまとめて、404 → 403 → 409 → 400 の順（common.md 3.20）に、先のもの1つを返す：
 * 借りたのが自分（403）、まだ返却されていない（409）、その備品が管理中（400）。
 */
@Getter
@AllArgsConstructor
@LoanBorrowedBySelf(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.Forbidden.class)
@LoanNotReturned(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.Conflict.class)
@LoanEquipmentManaged(groups = ServiceCheckOrder.Second.class, payload = ServiceErrorType.BusinessRule.class)
public class EquipmentMineUpdateReturnItemServiceInDto implements LoanBorrowedBySelf.Target,
		LoanNotReturned.Target, LoanEquipmentManaged.Target {

	/** 貸出履歴のID（パスの値）。貸出履歴の行を FOR UPDATE で押さえる（B-03 の8章） */
	@LoanExists(forUpdate = true, groups = ServiceCheckOrder.First.class, payload = ServiceErrorType.NotFound.class)
	private final Long loanId;

	/** メモ（前後の空白を取り除いたもの）。なければ null */
	private final String memo;

	/** ログインしている人のユーザID（借りた人との一致を確かめる。更新した人に入れる） */
	private final Long loginUserId;
}
