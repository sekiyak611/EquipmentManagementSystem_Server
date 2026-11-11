package jp.co.highjunk.equipment.dto.equipment;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * C-03 で受け取る値（詳細設計 equipment/C-03_備品を登録する.md の2.3・6章。画面設計 12章の項目）。
 * 入力チェックの前に、文字の項目を EquipmentInputs で直す（前後の空白を取り除き、空なら null）。
 * 管理状態・最終確認日は受け取らない（サーバーが決める）。購入日・保証期日の日付の決まりは EquipmentValidator で確かめる（今日の日付が要るため）。
 *
 * @param managementNo     管理番号（必須。30文字まで）
 * @param categoryId       種別のID（必須）
 * @param maker            メーカー（50文字まで）
 * @param model            モデル（100文字まで）
 * @param serialNo         シリアルNo（100文字まで）
 * @param size             サイズ（20文字まで）
 * @param resolution       解像度（20文字まで）
 * @param heightAdjustable 昇降（true＝あり、false＝なし、null＝未設定）
 * @param pivot            ピボット（同上）
 * @param purchasedOn      購入日
 * @param warrantyUntil    保証期日
 * @param note             備考（500文字まで）
 */
public record EquipmentCreateRequest(
		@NotBlank(message = "管理番号を入力してください")
		@Size(max = 30, message = "管理番号は30文字以内で入力してください")
		String managementNo,

		@NotNull(message = "種別を選んでください")
		Long categoryId,

		@Size(max = 50, message = "メーカーは50文字以内で入力してください")
		String maker,

		@Size(max = 100, message = "モデルは100文字以内で入力してください")
		String model,

		@Size(max = 100, message = "シリアルNoは100文字以内で入力してください")
		String serialNo,

		@Size(max = 20, message = "サイズは20文字以内で入力してください")
		String size,

		@Size(max = 20, message = "解像度は20文字以内で入力してください")
		String resolution,

		Boolean heightAdjustable,

		Boolean pivot,

		LocalDate purchasedOn,

		LocalDate warrantyUntil,

		@Size(max = 500, message = "備考は500文字以内で入力してください")
		String note) {

	public EquipmentCreateRequest {
		managementNo = EquipmentInputs.text(managementNo);
		maker = EquipmentInputs.text(maker);
		model = EquipmentInputs.text(model);
		serialNo = EquipmentInputs.text(serialNo);
		size = EquipmentInputs.text(size);
		resolution = EquipmentInputs.text(resolution);
		note = EquipmentInputs.text(note);
	}
}
