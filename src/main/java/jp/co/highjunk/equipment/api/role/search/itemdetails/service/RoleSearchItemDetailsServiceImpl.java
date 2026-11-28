package jp.co.highjunk.equipment.api.role.search.itemdetails.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.api.role.search.itemdetails.model.RoleSearchItemDetailsRestOutDto;
import jp.co.highjunk.equipment.common.error.NotFoundException;
import jp.co.highjunk.equipment.common.validation.ServiceInDtoChecker;
import jp.co.highjunk.equipment.dao.entity.Permission;
import jp.co.highjunk.equipment.dao.entity.PermissionExample;
import jp.co.highjunk.equipment.dao.entity.Role;
import jp.co.highjunk.equipment.dao.entity.RolePermission;
import jp.co.highjunk.equipment.dao.entity.RolePermissionExample;
import jp.co.highjunk.equipment.dao.mapper.PermissionMapper;
import jp.co.highjunk.equipment.dao.mapper.RoleMapper;
import jp.co.highjunk.equipment.dao.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;

/**
 * H-02 権限1つを返すの業務の処理の中身（詳細設計 role/H-02_権限1つを返す.md の4章）。
 * 権限・権限と権限項目の対応・権限項目は、テーブルごとに読み、ここでつなぐ（JOIN を使わない。CLAUDE.md 5章）。
 * 読むだけだが、ServiceInDto のチェックと3つの読み込みを同じ時点の内容にそろえるため、読み取り専用の
 * {@code @Transactional} を付ける（H-02 の8章・12章の No.5）。
 */
@Service
@RequiredArgsConstructor
public class RoleSearchItemDetailsServiceImpl implements RoleSearchItemDetailsService {

	/** 権限項目の並び順（データ設計 5.3 の表の上から） */
	private static final String ORDER_BY = "sort_order";

	private final ServiceInDtoChecker serviceInDtoChecker;

	private final RoleMapper roleMapper;

	private final RolePermissionMapper rolePermissionMapper;

	private final PermissionMapper permissionMapper;

	@Override
	@Transactional(readOnly = true)
	public RoleSearchItemDetailsRestOutDto searchDetails(RoleSearchItemDetailsServiceInDto serviceInDto) {
		serviceInDtoChecker.check(serviceInDto);

		Role role = roleMapper.selectByPrimaryKey(serviceInDto.getId());
		if (role == null) {
			// 確かめたあとに読めないことは、同じまとまりの中では起きない。起きたら見つからない扱いにする
			throw new NotFoundException();
		}

		RolePermissionExample rolePermissionExample = new RolePermissionExample();
		rolePermissionExample.createCriteria().andRoleIdEqualTo(role.getId());
		List<String> codes = rolePermissionMapper.selectByExample(rolePermissionExample).stream()
				.map(RolePermission::getPermissionCode)
				.toList();

		// コードごとに SQL を出さず、1回で、権限項目の並び順に読み直す（N+1 を防ぐ）。0件なら読まない
		List<String> orderedCodes = List.of();
		if (!codes.isEmpty()) {
			PermissionExample permissionExample = new PermissionExample();
			permissionExample.createCriteria().andCodeIn(codes);
			permissionExample.setOrderByClause(ORDER_BY);
			orderedCodes = permissionMapper.selectByExample(permissionExample).stream()
					.map(Permission::getCode)
					.toList();
		}
		return new RoleSearchItemDetailsRestOutDto(role.getId(), role.getName(), orderedCodes, role.getVersion());
	}
}
