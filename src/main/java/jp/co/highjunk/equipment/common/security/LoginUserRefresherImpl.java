package jp.co.highjunk.equipment.common.security;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.dao.entity.RolePermission;
import jp.co.highjunk.equipment.dao.entity.RolePermissionExample;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.entity.UserExample;
import jp.co.highjunk.equipment.dao.mapper.RolePermissionMapper;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

/**
 * APIを呼ぶたびの権限項目の読み直し（LoginUserRefresher の中身。docs/API設計.md 2.2、詳細設計 common.md 3.10）。
 * API でない処理なので、common に置く（CLAUDE.md 4.1。2026-11-15 に auth から移した）。
 */
@Service
@RequiredArgsConstructor
public class LoginUserRefresherImpl implements LoginUserRefresher {

	private static final Logger log = LoggerFactory.getLogger(LoginUserRefresherImpl.class);

	private final UserMapper userMapper;

	private final RolePermissionMapper rolePermissionMapper;

	/**
	 * ログインしている人の今の権限項目。
	 * ユーザが削除された、またはログインした日時が session_valid_after より前なら、空（ログアウトさせる）。
	 */
	@Override
	public Optional<List<String>> currentPermissions(LoginUser loginUser) {
		UserExample userExample = new UserExample();
		userExample.createCriteria().andIdEqualTo(loginUser.userId()).andDeletedAtIsNull();
		List<User> users = userMapper.selectByExample(userExample);
		if (users.isEmpty()) {
			log.info("削除されたユーザのため、ログアウトさせました（userId={}）", loginUser.userId());
			return Optional.empty();
		}
		User user = users.get(0);
		if (user.getSessionValidAfter() != null && loginUser.loggedInAt().isBefore(user.getSessionValidAfter())) {
			log.info("パスワードが変わったため、ログアウトさせました（userId={}）", loginUser.userId());
			return Optional.empty();
		}
		RolePermissionExample example = new RolePermissionExample();
		example.createCriteria().andRoleIdEqualTo(user.getRoleId());
		return Optional.of(rolePermissionMapper.selectByExample(example).stream()
				.map(RolePermission::getPermissionCode)
				.toList());
	}
}
