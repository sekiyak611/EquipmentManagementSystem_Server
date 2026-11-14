package jp.co.highjunk.equipment.api.equipment.category.entry.item.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jp.co.highjunk.equipment.common.text.TextInputs;
import lombok.Getter;

/**
 * K-02 で画面から受け取る値と、形式のチェック（詳細設計 equipment/category/K-02_種別を追加する.md の2.3・6章）。
 * 形式のチェックの前に、名前の前後の空白を取り除き、頭文字を大文字にする（空なら null）。
 */
@Getter
public class EquipmentCategoryEntryItemRestInDto {

	/** 名前（必須。20文字まで） */
	@NotBlank(message = "名前を入力してください")
	@Size(max = 20, message = "名前は20文字以内で入力してください")
	private final String name;

	/** 頭文字（任意。半角の英字1文字）。なければ null */
	@Pattern(regexp = "[A-Z]", message = "頭文字は英字1文字で入力してください")
	private final String initial;

	@JsonCreator
	public EquipmentCategoryEntryItemRestInDto(@JsonProperty("name") String name,
			@JsonProperty("initial") String initial) {
		this.name = TextInputs.strip(name);
		this.initial = TextInputs.stripToUpperCaseOrNull(initial);
	}
}
