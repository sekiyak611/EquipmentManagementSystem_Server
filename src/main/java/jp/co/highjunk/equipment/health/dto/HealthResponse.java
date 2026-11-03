package jp.co.highjunk.equipment.health.dto;

/**
 * A-08 で返す data の中身。版や設定などの詳しい情報は入れない（docs/API設計.md 3.1）。
 *
 * @param state 動いていれば UP
 */
public record HealthResponse(String state) {

	public static HealthResponse up() {
		return new HealthResponse("UP");
	}
}
