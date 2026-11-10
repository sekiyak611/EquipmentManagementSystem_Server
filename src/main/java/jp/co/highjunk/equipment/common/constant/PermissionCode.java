package jp.co.highjunk.equipment.common.constant;

/**
 * 権限項目のコード（docs/データ設計.md 5.3。permissions.code と同じ文字）。
 * 各 API の @PreAuthorize("hasAuthority('EQUIPMENT_UPDATE')") の文字も、このコードと同じにする（CLAUDE.md 5章）。
 */
public enum PermissionCode {

	/** 備品一覧を見る */
	EQUIPMENT_VIEW,
	/** 備品を登録する */
	EQUIPMENT_CREATE,
	/** 備品を編集する */
	EQUIPMENT_UPDATE,
	/** 備品を削除する */
	EQUIPMENT_DELETE,
	/** 種別を管理する */
	CATEGORY_MANAGE,
	/** 備品を借りる・返す（自分） */
	LOAN_SELF,
	/** 貸出・返却を管理する */
	LOAN_MANAGE,
	/** 貸出履歴を見る */
	LOAN_HISTORY_VIEW,
	/** 最終確認日を更新する（自分） */
	CONFIRM_SELF,
	/** 最終確認日を更新する（すべて） */
	CONFIRM_ALL,
	/** 最終確認日をCSVで一括更新する */
	CONFIRM_CSV,
	/** 催促履歴を見る */
	REMINDER_LOG_VIEW,
	/** ユーザを見る */
	USER_VIEW,
	/** ユーザを登録する */
	USER_CREATE,
	/** ユーザを編集する */
	USER_UPDATE,
	/** ユーザを削除する */
	USER_DELETE,
	/** 権限を見る */
	PERMISSION_VIEW,
	/** 権限を管理する */
	PERMISSION_MANAGE
}
