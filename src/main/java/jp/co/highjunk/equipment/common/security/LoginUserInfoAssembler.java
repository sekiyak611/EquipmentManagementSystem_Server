package jp.co.highjunk.equipment.common.security;

import java.util.List;

import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.constant.HomePage;
import jp.co.highjunk.equipment.common.constant.PermissionCode;
import jp.co.highjunk.equipment.dao.entity.Permission;
import jp.co.highjunk.equipment.dao.entity.PermissionExample;
import jp.co.highjunk.equipment.dao.entity.Role;
import jp.co.highjunk.equipment.dao.entity.RolePermission;
import jp.co.highjunk.equipment.dao.entity.RolePermissionExample;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.mapper.PermissionMapper;
import jp.co.highjunk.equipment.dao.mapper.RoleMapper;
import jp.co.highjunk.equipment.dao.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;

/**
 * ユーザの行から、ログインしている人の情報と権限項目（LoginUserInfo）を作る（詳細設計 common.md 3.10）。
 * A-01（ログインできた人を返す）と A-03（ログインしている人を返す）の2つの API で使うので、common に置く（CLAUDE.md 4.1。2026-11-15 に決めた）。
 * 読むだけ。JOIN を使わず、テーブルごとに読む（CLAUDE.md 5章）。
 */
@Component
@RequiredArgsConstructor
public class LoginUserInfoAssembler {

	private final RoleMapper roleMapper;

	private final RolePermissionMapper rolePermissionMapper;

	private final PermissionMapper permissionMapper;

	public LoginUserInfo assemble(User user) {
		List<String> permissions = permissionCodes(user.getRoleId());
		HomePage homePage = permissions.contains(PermissionCode.EQUIPMENT_VIEW.name())
				? HomePage.EQUIPMENT_LIST
				: HomePage.MY_EQUIPMENT;
		Role role = roleMapper.selectByPrimaryKey(user.getRoleId());
		return new LoginUserInfo(user.getId(), user.getName(), user.getEmail(), role == null ? null : role.getName(),
				permissions, homePage);
	}

	/** 権限に含まれる権限項目のコードを、権限項目の並び順（sort_order）で返す。 */
	private List<String> permissionCodes(long roleId) {
		RolePermissionExample rolePermissionExample = new RolePermissionExample();
		rolePermissionExample.createCriteria().andRoleIdEqualTo(roleId);
		List<String> codes = rolePermissionMapper.selectByExample(rolePermissionExample).stream()
				.map(RolePermission::getPermissionCode)
				.toList();
		if (codes.isEmpty()) {
			return List.of();
		}
		PermissionExample permissionExample = new PermissionExample();
		permissionExample.createCriteria().andCodeIn(codes);
		permissionExample.setOrderByClause("`sort_order`");
		return permissionMapper.selectByExample(permissionExample).stream().map(Permission::getCode).toList();
	}
}
