package jp.co.highjunk.equipment.api.equipment.management.entry.item.controller;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jp.co.highjunk.equipment.common.text.TextInputs;
import lombok.Getter;

/**
 * C-03 で画面から受け取る値と、形式のチェック（詳細設計 equipment/management/C-03_備品を登録する.md の2.3・6章。画面設計 12章の項目）。
 * 形式のチェックの前に、文字の項目を直す（前後の空白を取り除き、空なら null。common.text.TextInputs）。
 * 管理状態・最終確認日は受け取らない（サーバーが決める）。購入日・保証期日の日付の決まりは Validator で確かめる（今日の日付が要るため）。
 */
@Getter
public class EquipmentManagementEntryItemRestInDto {

	/** 管理番号（必須。30文字まで） */
	@NotBlank(message = "管理番号を入力してください")
	@Size(max = 30, message = "管理番号は30文字以内で入力してください")
	private final String managementNo;

	/** 種別のID（必須） */
	@NotNull(message = "種別を選んでください")
	private final Long categoryId;

	/** メーカー（50文字まで） */
	@Size(max = 50, message = "メーカーは50文字以内で入力してください")
	private final String maker;

	/** モデル（100文字まで） */
	@Size(max = 100, message = "モデルは100文字以内で入力してください")
	private final String model;

	/** シリアルNo（100文字まで） */
	@Size(max = 100, message = "シリアルNoは100文字以内で入力してください")
	private final String serialNo;

	/** サイズ（20文字まで） */
	@Size(max = 20, message = "サイズは20文字以内で入力してください")
	private final String size;

	/** 解像度（20文字まで） */
	@Size(max = 20, message = "解像度は20文字以内で入力してください")
	private final String resolution;

	/** 昇降（true＝あり、false＝なし、null＝未設定） */
	private final Boolean heightAdjustable;

	/** ピボット（同上） */
	private final Boolean pivot;

	/** 購入日 */
	private final LocalDate purchasedOn;

	/** 保証期日 */
	private final LocalDate warrantyUntil;

	/** 備考（500文字まで） */
	@Size(max = 500, message = "備考は500文字以内で入力してください")
	private final String note;

	@JsonCreator
	public EquipmentManagementEntryItemRestInDto(@JsonProperty("managementNo") String managementNo,
			@JsonProperty("categoryId") Long categoryId, @JsonProperty("maker") String maker,
			@JsonProperty("model") String model, @JsonProperty("serialNo") String serialNo,
			@JsonProperty("size") String size, @JsonProperty("resolution") String resolution,
			@JsonProperty("heightAdjustable") Boolean heightAdjustable, @JsonProperty("pivot") Boolean pivot,
			@JsonProperty("purchasedOn") LocalDate purchasedOn, @JsonProperty("warrantyUntil") LocalDate warrantyUntil,
			@JsonProperty("note") String note) {
		this.managementNo = TextInputs.stripToNull(managementNo);
		this.categoryId = categoryId;
		this.maker = TextInputs.stripToNull(maker);
		this.model = TextInputs.stripToNull(model);
		this.serialNo = TextInputs.stripToNull(serialNo);
		this.size = TextInputs.stripToNull(size);
		this.resolution = TextInputs.stripToNull(resolution);
		this.heightAdjustable = heightAdjustable;
		this.pivot = pivot;
		this.purchasedOn = purchasedOn;
		this.warrantyUntil = warrantyUntil;
		this.note = TextInputs.stripToNull(note);
	}
}
