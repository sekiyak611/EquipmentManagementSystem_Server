package jp.co.highjunk.equipment.dto.category;

/**
 * 種別の1件の返す値（詳細設計 category/K-01_種別一覧.md の3章）。K-01 は、data にこれの配列を入れる。
 *
 * @param id             種別のID
 * @param name           名前
 * @param initial        頭文字（半角の英大文字1文字）。ない種別は null
 * @param equipmentCount 使っている備品の数（削除されていない備品。管理状態は問わない）
 * @param version        種別を変える（K-03）ときに送り返す番号（API設計 2.7 の楽観ロック）
 */
public record CategoryResponse(long id, String name, String initial, long equipmentCount, int version) {
}
