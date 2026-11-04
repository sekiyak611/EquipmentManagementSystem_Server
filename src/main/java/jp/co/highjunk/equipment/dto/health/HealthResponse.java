package jp.co.highjunk.equipment.dto.health;

import jp.co.highjunk.equipment.common.constant.HealthState;

/**
 * A-08 で返す data の中身。版や設定などの詳しい情報は入れない（docs/API設計.md 3.1）。
 * dto は機能のパッケージの外の dto.<機能名> に置く（CLAUDE.md 4.1）。
 *
 * @param state 動いていれば UP（JSON では文字の "UP" になる）
 */
public record HealthResponse(HealthState state) {

	public static HealthResponse up() {
		return new HealthResponse(HealthState.UP);
	}
}
