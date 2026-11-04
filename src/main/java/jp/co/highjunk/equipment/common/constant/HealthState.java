package jp.co.highjunk.equipment.common.constant;

/**
 * A-08 で返す state の値（docs/API設計.md 3.1）。
 * Enum はすべて common.constant に置く（CLAUDE.md 4.1）。
 * JSON では、Enum の名前がそのまま文字になる（UP → "UP"）。
 */
public enum HealthState {

	/** アプリとデータベースが動いている */
	UP
}
