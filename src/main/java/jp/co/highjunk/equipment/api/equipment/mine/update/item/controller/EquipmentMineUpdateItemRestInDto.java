package jp.co.highjunk.equipment.api.equipment.mine.update.item.controller;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

/**
 * B-04 で画面から受け取る値と、形式のチェック（詳細設計 equipment/mine/B-04_確認した.md の2.3・6章）。
 * 日付は受け取らない（最終確認日は、サーバーが今日の日付を入れる。送られても使わない）。
 */
@Getter
public class EquipmentMineUpdateItemRestInDto {

	/** 確認する備品のID（1つ以上。1件のときも配列で送る）。重なりは、ServiceImpl が1つにまとめる */
	@NotEmpty(message = "確認する備品を選んでください")
	private final List<Long> equipmentIds;

	@JsonCreator
	public EquipmentMineUpdateItemRestInDto(@JsonProperty("equipmentIds") List<Long> equipmentIds) {
		// 中身が null のときは、JSON の形の誤りと同じ 400（errors は null）にする（B-04 の12章の No.11。K-03 の version と同じ扱い）。
		// ここで投げた例外は、JSON の読み取りの誤り（HttpMessageNotReadableException）として扱われる
		if (equipmentIds != null && equipmentIds.contains(null)) {
			throw new IllegalArgumentException("equipmentIds の中に null があります");
		}
		this.equipmentIds = equipmentIds == null ? null : List.copyOf(equipmentIds);
	}
}
