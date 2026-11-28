package jp.co.highjunk.equipment.api.role.search.itemdetails.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * H-02 で返す権限1つ（詳細設計 role/H-02_権限1つを返す.md の3章）。data にこれを入れる。
 */
@Getter
@AllArgsConstructor
public class RoleSearchItemDetailsRestOutDto {

	/** 権限のID */
	private final long id;

	/** 権限の名前 */
	private final String name;

	/** この権限が含む権限項目のコード（データ設計 5.3 のコード）。権限項目の並び順（permissions.sort_order）。0件なら空 */
	private final List<String> permissionCodes;

	/** 権限を変える（H-04）ときに送り返す番号（API設計 2.7 の楽観ロック） */
	private final int version;
}
