package jp.co.highjunk.equipment.api.equipment.mine.update.returnitem.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;
import jp.co.highjunk.equipment.common.text.TextInputs;
import lombok.Getter;

/**
 * B-03 で画面から受け取る値と、形式のチェック（詳細設計 equipment/mine/B-03_備品を返す.md の2.3・6章）。
 * 返却日は受け取らない（送られても使わない。サーバーが今日の日付を入れる）。
 */
@Getter
public class EquipmentMineUpdateReturnItemRestInDto {

	/** メモ（任意。200文字まで）。前後の空白を取り、空なら null（形式のチェックの前に直す。12章の No.4） */
	@Size(max = 200, message = "メモは200文字以内で入力してください")
	private final String memo;

	@JsonCreator
	public EquipmentMineUpdateReturnItemRestInDto(@JsonProperty("memo") String memo) {
		this.memo = TextInputs.stripToNull(memo);
	}
}
