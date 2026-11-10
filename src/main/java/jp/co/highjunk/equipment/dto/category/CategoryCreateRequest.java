package jp.co.highjunk.equipment.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * K-02 で受け取る値（詳細設計 category/K-02_種別を追加する.md の2.3・6章）。
 * 入力チェックの前に、名前・頭文字を CategoryInputs で直す（前後の空白を取り除き、頭文字を大文字にする。空なら null）。
 *
 * @param name    名前（必須。20文字まで）
 * @param initial 頭文字（任意。半角の英字1文字）。なければ null
 */
public record CategoryCreateRequest(
		@NotBlank(message = "名前を入力してください")
		@Size(max = 20, message = "名前は20文字以内で入力してください")
		String name,

		@Pattern(regexp = "[A-Z]", message = "頭文字は英字1文字で入力してください")
		String initial) {

	public CategoryCreateRequest {
		name = CategoryInputs.name(name);
		initial = CategoryInputs.initial(initial);
	}
}
