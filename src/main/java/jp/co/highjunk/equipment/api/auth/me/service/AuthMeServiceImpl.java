package jp.co.highjunk.equipment.api.auth.me.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.api.auth.me.model.AuthMeRestOutDto;
import jp.co.highjunk.equipment.common.error.ApiMessages;
import jp.co.highjunk.equipment.common.error.UnauthorizedException;
import jp.co.highjunk.equipment.common.security.LoginUserInfoAssembler;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.entity.UserExample;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

/**
 * A-03 ログイン中の人の情報の業務の処理の中身（詳細設計 auth/A-03_ログイン中の人の情報.md の4章）。
 * 返す値は common.security.LoginUserInfoAssembler で作る（A-01 と同じ形）。読むだけ。
 */
@Service
@RequiredArgsConstructor
public class AuthMeServiceImpl implements AuthMeService {

	private final UserMapper userMapper;

	private final LoginUserInfoAssembler loginUserInfoAssembler;

	@Override
	public AuthMeRestOutDto me(AuthMeServiceInDto serviceInDto) {
		UserExample example = new UserExample();
		example.createCriteria().andIdEqualTo(serviceInDto.getLoginUserId()).andDeletedAtIsNull();
		List<User> users = userMapper.selectByExample(example);
		if (users.isEmpty()) {
			throw new UnauthorizedException(ApiMessages.UNAUTHORIZED);
		}
		return AuthMeRestOutDto.from(loginUserInfoAssembler.assemble(users.get(0)));
	}
}
