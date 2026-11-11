package jp.co.highjunk.equipment.dto.equipment;

import java.util.List;

/**
 * C-01 の返す値（API設計 2.5 のページ分けの形。詳細設計 equipment/C-01_備品を探す.md の3章）。
 *
 * @param items      このページの備品
 * @param page       何ページ目か
 * @param size       1ページの件数（50）
 * @param totalCount 条件に合う備品の数
 * @param totalPages ページの数（0件なら 0）
 */
public record EquipmentListResponse(List<EquipmentListItem> items, int page, int size, long totalCount,
		int totalPages) {
}
