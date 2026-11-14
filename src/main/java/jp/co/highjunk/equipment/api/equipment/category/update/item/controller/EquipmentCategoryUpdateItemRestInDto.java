package jp.co.highjunk.equipment.api.equipment.category.update.item.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jp.co.highjunk.equipment.common.text.TextInputs;
import lombok.Getter;

/**
 * K-03 で画面から受け取る値と、形式のチェック（詳細設計 equipment/category/K-03_種別を変える.md の2.3・6章）。
 * name・initial は K-02 と同じ決まりで、形式のチェックの前に直す。
 */
@Getter
public class EquipmentCategoryUpdateItemRestInDto {

	/** 名前（必須。20文字まで） */
	@NotBlank(message = "名前を入力してください")
	@Size(max = 20, message = "名前は20文字以内で入力してください")
	private final String name;

	/** 頭文字（任意。半角の英字1文字）。なければ null */
	@Pattern(regexp = "[A-Z]", message = "頭文字は英字1文字で入力してください")
	private final String initial;

	/** 画面を開いたときに受け取った version（楽観ロック。API設計 2.7）。必須 */
	private final Integer version;

	@JsonCreator
	public EquipmentCategoryUpdateItemRestInDto(@JsonProperty("name") String name,
			@JsonProperty("initial") String initial, @JsonProperty("version") Integer version) {
		// version がないときは、JSON の形の誤りと同じ 400（errors は null）にする（K-03 の12章の No.4）。
		// ここで投げた例外は、JSON の読み取りの誤り（HttpMessageNotReadableException）として扱われる
		if (version == null) {
			throw new IllegalArgumentException("version がありません");
		}
		this.name = TextInputs.strip(name);
		this.initial = TextInputs.stripToUpperCaseOrNull(initial);
		this.version = version;
	}
}
