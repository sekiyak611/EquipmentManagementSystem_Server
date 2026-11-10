package jp.co.highjunk.equipment.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.auth.model.User;
import jp.co.highjunk.equipment.auth.validator.LoginValidator;
import jp.co.highjunk.equipment.dto.auth.LoginRequest;
import jp.co.highjunk.equipment.dto.auth.MeResponse;

/**
 * A-01 ログインの業務のルールの中身（詳細設計 auth/A-01_ログイン.md）。
 * 返す値は A-03 と同じ形なので、同じ機能の MeService で作る（CLAUDE.md 5章：同じ機能の中なら、ほかの API の service を使ってよい）。
 */
@Service
public class LoginServiceImpl implements LoginService {

	private static final Logger log = LoggerFactory.getLogger(LoginServiceImpl.class);

	private final LoginValidator loginValidator;

	private final MeService meService;

	public LoginServiceImpl(LoginValidator loginValidator, MeService meService) {
		this.loginValidator = loginValidator;
		this.meService = meService;
	}

	@Override
	public MeResponse login(LoginRequest request) {
		User user = loginValidator.validate(request.email(), request.password());
		log.info("ログインしました（userId={}）", user.getId());
		return meService.toMeResponse(user);
	}
}
