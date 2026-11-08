package jp.co.highjunk.equipment.auth.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.auth.model.User;
import jp.co.highjunk.equipment.auth.validator.LoginValidator;
import jp.co.highjunk.equipment.common.constant.HomePage;
import jp.co.highjunk.equipment.common.constant.PermissionCode;
import jp.co.highjunk.equipment.common.error.ApiMessages;
import jp.co.highjunk.equipment.common.error.UnauthorizedException;
import jp.co.highjunk.equipment.common.security.LoginUser;
import jp.co.highjunk.equipment.common.security.LoginUserRefresher;
import jp.co.highjunk.equipment.dto.auth.LoginRequest;
import jp.co.highjunk.equipment.dto.auth.MeResponse;
import jp.co.highjunk.equipment.mapper.auth.AuthMapper;

/**
 * auth 機能の業務のルールの中身（詳細設計 auth/A-01_ログイン.md・A-03_ログイン中の人の情報.md）。
 * APIを呼ぶたびの権限項目の読み直し（common.security.LoginUserRefresher）も受け持つ。
 */
@Service
public class AuthServiceImpl implements AuthService, LoginUserRefresher {

	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

	private final AuthMapper authMapper;

	private final LoginValidator loginValidator;

	public AuthServiceImpl(AuthMapper authMapper, LoginValidator loginValidator) {
		this.authMapper = authMapper;
		this.loginValidator = loginValidator;
	}

	@Override
	public MeResponse login(LoginRequest request) {
		User user = loginValidator.validate(request.email(), request.password());
		log.info("ログインしました（userId={}）", user.getId());
		return toMeResponse(user);
	}

	@Override
	public MeResponse me(long userId) {
		User user = authMapper.selectUserById(userId);
		if (user == null) {
			throw new UnauthorizedException(ApiMessages.UNAUTHORIZED);
		}
		return toMeResponse(user);
	}

	/**
	 * ログインしている人の今の権限項目（docs/API設計.md 2.2）。
	 * ユーザが削除された、またはログインした日時が session_valid_after より前なら、空（ログアウトさせる）。
	 */
	@Override
	public Optional<List<String>> currentPermissions(LoginUser loginUser) {
		User user = authMapper.selectUserById(loginUser.userId());
		if (user == null) {
			log.info("削除されたユーザのため、ログアウトさせました（userId={}）", loginUser.userId());
			return Optional.empty();
		}
		if (user.getSessionValidAfter() != null && loginUser.loggedInAt().isBefore(user.getSessionValidAfter())) {
			log.info("パスワードが変わったため、ログアウトさせました（userId={}）", loginUser.userId());
			return Optional.empty();
		}
		return Optional.of(authMapper.selectPermissionCodes(user.getRoleId()));
	}

	private MeResponse toMeResponse(User user) {
		List<String> codes = authMapper.selectPermissionCodes(user.getRoleId());
		List<String> permissions = codes.isEmpty() ? List.of() : authMapper.selectSortedPermissionCodes(codes);
		HomePage homePage = permissions.contains(PermissionCode.EQUIPMENT_VIEW.name())
				? HomePage.EQUIPMENT_LIST
				: HomePage.MY_EQUIPMENT;
		return new MeResponse(user.getId(), user.getName(), user.getEmail(), authMapper.selectRoleName(user.getRoleId()),
				permissions, homePage);
	}
}
