package jp.co.highjunk.equipment.api.auth.login.service;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.api.auth.login.model.AuthLoginRestOutDto;
import jp.co.highjunk.equipment.api.auth.login.validator.AuthLoginValidator;
import jp.co.highjunk.equipment.api.auth.login.validator.AuthLoginValidatorDto;
import jp.co.highjunk.equipment.common.security.LoginUserInfoAssembler;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.entity.UserExample;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

/**
 * A-01 ログインの業務の処理の中身（詳細設計 auth/A-01_ログイン.md の4章）。
 * 返す値は A-03 と同じ形なので、common.security.LoginUserInfoAssembler で作る（API どうしは使い合わない。CLAUDE.md 5章）。
 * 書き換えないので @Transactional は付けない。
 */
@Service
@RequiredArgsConstructor
public class AuthLoginServiceImpl implements AuthLoginService {

	private static final Logger log = LoggerFactory.getLogger(AuthLoginServiceImpl.class);

	private final UserMapper userMapper;

	private final AuthLoginValidator validator;

	private final LoginUserInfoAssembler loginUserInfoAssembler;

	@Override
	public AuthLoginRestOutDto login(AuthLoginServiceInDto serviceInDto) {
		// メールアドレスは小文字にそろえて保存している（データ設計 2章）
		UserExample example = new UserExample();
		example.createCriteria().andEmailEqualTo(serviceInDto.getEmail().toLowerCase(Locale.ROOT))
				.andDeletedAtIsNull();
		List<User> users = userMapper.selectByExample(example);
		User user = users.isEmpty() ? null : users.get(0);

		validator.validate(new AuthLoginValidatorDto(user, serviceInDto.getPassword()));

		log.info("ログインしました（userId={}）", user.getId());
		return AuthLoginRestOutDto.from(loginUserInfoAssembler.assemble(user));
	}
}
