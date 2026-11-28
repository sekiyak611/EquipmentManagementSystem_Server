package jp.co.highjunk.equipment.api.role.search.itemdetails.service;

import jp.co.highjunk.equipment.api.role.search.itemdetails.model.RoleSearchItemDetailsRestOutDto;

/**
 * H-02 権限1つを返すの業務の処理（中身は RoleSearchItemDetailsServiceImpl。詳細設計 role/H-02_権限1つを返す.md）。
 */
public interface RoleSearchItemDetailsService {

	/**
	 * 削除されていない権限を、含む権限項目のコードつきで返す。コードは権限項目の並び順。
	 * 権限がない・削除済みなら、404（NotFoundException）。
	 */
	RoleSearchItemDetailsRestOutDto searchDetails(RoleSearchItemDetailsServiceInDto serviceInDto);
}
