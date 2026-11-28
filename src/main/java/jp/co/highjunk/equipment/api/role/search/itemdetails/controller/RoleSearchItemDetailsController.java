package jp.co.highjunk.equipment.api.role.search.itemdetails.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.api.role.search.itemdetails.model.RoleSearchItemDetailsRestOutDto;
import jp.co.highjunk.equipment.api.role.search.itemdetails.service.RoleSearchItemDetailsService;
import jp.co.highjunk.equipment.api.role.search.itemdetails.service.RoleSearchItemDetailsServiceInDto;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import lombok.RequiredArgsConstructor;

/**
 * H-02 権限1つを返す（詳細設計 role/H-02_権限1つを返す.md）。
 */
@RestController
@RequiredArgsConstructor
public class RoleSearchItemDetailsController {

	private final RoleSearchItemDetailsService service;

	/**
	 * PERMISSION_VIEW か PERMISSION_MANAGE のどちらかを持っていれば呼べる（S-17 を開ける人が、中の読み込みで 403 にならないように。API設計 3.8）。
	 * パスの id が数でないときは、権限の確認より前に 400 になる（H-02 の9章）。
	 */
	@GetMapping("/api/roles/{id}")
	@PreAuthorize("hasAnyAuthority('PERMISSION_VIEW', 'PERMISSION_MANAGE')")
	public ApiResponse<RoleSearchItemDetailsRestOutDto> searchDetails(@PathVariable long id) {
		return ApiResponse.ok(service.searchDetails(new RoleSearchItemDetailsServiceInDto(id)));
	}
}
