package jp.co.highjunk.equipment.api.health.model;

import jp.co.highjunk.equipment.common.constant.HealthState;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A-08 で返す data の中身。版や設定などの詳しい情報は入れない（docs/API設計.md 3.1）。
 */
@Getter
@AllArgsConstructor
public class HealthRestOutDto {

	/** 動いていれば UP（JSON では文字の "UP" になる） */
	private final HealthState state;
}
