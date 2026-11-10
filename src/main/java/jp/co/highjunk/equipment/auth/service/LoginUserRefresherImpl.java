package jp.co.highjunk.equipment.auth.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.auth.model.User;
import jp.co.highjunk.equipment.common.security.LoginUser;
import jp.co.highjunk.equipment.common.security.LoginUserRefresher;
import jp.co.highjunk.equipment.mapper.auth.RolePermissionMapper;
import jp.co.highjunk.equipment.mapper.auth.UserMapper;

/**
 * APIを呼ぶたびの権限項目の読み直し（common.security.LoginUserRefresher の中身。docs/API設計.md 2.2、詳細設計 auth/auth.md）。
 * API ではないので、controller はない。common から機能を使わないため、約束（インターフェース）だけを common に置き、中身をここに置く。
 */
@Service
public class LoginUserRefresherImpl implements LoginUserRefresher {

	private static final Logger log = LoggerFactory.getLogger(LoginUserRefresherImpl.class);

	private final UserMapper userMapper;

	private final RolePermissionMapper rolePermissionMapper;

	public LoginUserRefresherImpl(UserMapper userMapper, RolePermissionMapper rolePermissionMapper) {
		this.userMapper = userMapper;
		this.rolePermissionMapper = rolePermissionMapper;
	}

	/**
	 * ログインしている人の今の権限項目。
	 * ユーザが削除された、またはログインした日時が session_valid_after より前なら、空（ログアウトさせる）。
	 */
	@Override
	public Optional<List<String>> currentPermissions(LoginUser loginUser) {
		User user = userMapper.selectById(loginUser.userId());
		if (user == null) {
			log.info("削除されたユーザのため、ログアウトさせました（userId={}）", loginUser.userId());
			return Optional.empty();
		}
		if (user.getSessionValidAfter() != null && loginUser.loggedInAt().isBefore(user.getSessionValidAfter())) {
			log.info("パスワードが変わったため、ログアウトさせました（userId={}）", loginUser.userId());
			return Optional.empty();
		}
		return Optional.of(rolePermissionMapper.selectPermissionCodesByRoleId(user.getRoleId()));
	}
}
