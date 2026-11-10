package jp.co.highjunk.equipment.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * K-03 で受け取る値（詳細設計 category/K-03_種別を変える.md の2.3・6章）。
 * name・initial は K-02 と同じ決まりで、入力チェックの前に CategoryInputs で直す。
 *
 * @param name    名前（必須。20文字まで）
 * @param initial 頭文字（任意。半角の英字1文字）。なければ null
 * @param version 画面を開いたときに受け取った version（楽観ロック。API設計 2.7）。必須
 */
public record CategoryUpdateRequest(
		@NotBlank(message = "名前を入力してください")
		@Size(max = 20, message = "名前は20文字以内で入力してください")
		String name,

		@Pattern(regexp = "[A-Z]", message = "頭文字は英字1文字で入力してください")
		String initial,

		Integer version) {

	public CategoryUpdateRequest {
		// version がないときは、JSON の形の誤りと同じ 400（errors は null）にする（K-03 の12章の No.4）。
		// ここで投げた例外は、JSON の読み取りの誤り（HttpMessageNotReadableException）として扱われる
		if (version == null) {
			throw new IllegalArgumentException("version がありません");
		}
		name = CategoryInputs.name(name);
		initial = CategoryInputs.initial(initial);
	}
}
