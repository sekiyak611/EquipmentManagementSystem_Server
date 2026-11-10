package jp.co.highjunk.equipment.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.auth.model.User;
import jp.co.highjunk.equipment.common.constant.HomePage;
import jp.co.highjunk.equipment.common.constant.PermissionCode;
import jp.co.highjunk.equipment.common.error.ApiMessages;
import jp.co.highjunk.equipment.common.error.UnauthorizedException;
import jp.co.highjunk.equipment.dto.auth.MeResponse;
import jp.co.highjunk.equipment.mapper.auth.PermissionMapper;
import jp.co.highjunk.equipment.mapper.auth.RoleMapper;
import jp.co.highjunk.equipment.mapper.auth.RolePermissionMapper;
import jp.co.highjunk.equipment.mapper.auth.UserMapper;

/**
 * A-03 ログイン中の人の情報の業務のルールの中身（詳細設計 auth/A-03_ログイン中の人の情報.md）。
 */
@Service
public class MeServiceImpl implements MeService {

	private final UserMapper userMapper;

	private final RoleMapper roleMapper;

	private final RolePermissionMapper rolePermissionMapper;

	private final PermissionMapper permissionMapper;

	public MeServiceImpl(UserMapper userMapper, RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper,
			PermissionMapper permissionMapper) {
		this.userMapper = userMapper;
		this.roleMapper = roleMapper;
		this.rolePermissionMapper = rolePermissionMapper;
		this.permissionMapper = permissionMapper;
	}

	@Override
	public MeResponse me(long userId) {
		User user = userMapper.selectById(userId);
		if (user == null) {
			throw new UnauthorizedException(ApiMessages.UNAUTHORIZED);
		}
		return toMeResponse(user);
	}

	@Override
	public MeResponse toMeResponse(User user) {
		List<String> codes = rolePermissionMapper.selectPermissionCodesByRoleId(user.getRoleId());
		List<String> permissions = codes.isEmpty() ? List.of() : permissionMapper.selectSortedCodes(codes);
		HomePage homePage = permissions.contains(PermissionCode.EQUIPMENT_VIEW.name())
				? HomePage.EQUIPMENT_LIST
				: HomePage.MY_EQUIPMENT;
		return new MeResponse(user.getId(), user.getName(), user.getEmail(), roleMapper.selectNameById(user.getRoleId()),
				permissions, homePage);
	}
}
