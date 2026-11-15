package jp.co.highjunk.equipment.api.auth.login.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.api.auth.login.model.AuthLoginRestOutDto;
import jp.co.highjunk.equipment.api.auth.login.validator.AuthLoginValidator;
import jp.co.highjunk.equipment.api.auth.login.validator.AuthLoginValidatorDto;
import jp.co.highjunk.equipment.common.error.UnauthorizedException;
import jp.co.highjunk.equipment.common.passwordlink.IssuedPasswordLink;
import jp.co.highjunk.equipment.common.security.AccountLockNotifier;
import jp.co.highjunk.equipment.common.security.LoginFailureRecorder;
import jp.co.highjunk.equipment.common.security.LoginUserInfoAssembler;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.entity.UserExample;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

/**
 * A-01 ログインの業務の処理の中身（詳細設計 auth/A-01_ログイン.md の4章）。
 * 返す値は A-03 と同じ形なので、common.security.LoginUserInfoAssembler で作る（API どうしは使い合わない。CLAUDE.md 5章）。
 * このメソッドには @Transactional を付けない。失敗は UnauthorizedException で終わり、付けると、数えた失敗の回数も
 * 一緒に元に戻ってしまうため。数える・0に戻すのは、別のクラス LoginFailureRecorder が、自分のまとまりで行う。
 */
@Service
@RequiredArgsConstructor
public class AuthLoginServiceImpl implements AuthLoginService {

	private static final Logger log = LoggerFactory.getLogger(AuthLoginServiceImpl.class);

	private final UserMapper userMapper;

	private final AuthLoginValidator validator;

	private final LoginUserInfoAssembler loginUserInfoAssembler;

	private final LoginFailureRecorder failureRecorder;

	private final AccountLockNotifier lockNotifier;

	@Override
	public AuthLoginRestOutDto login(AuthLoginServiceInDto serviceInDto) {
		// メールアドレスは小文字にそろえて保存している（データ設計 2章）
		UserExample example = new UserExample();
		example.createCriteria().andEmailEqualTo(serviceInDto.getEmail().toLowerCase(Locale.ROOT))
				.andDeletedAtIsNull();
		List<User> users = userMapper.selectByExample(example);
		User user = users.isEmpty() ? null : users.get(0);

		try {
			validator.validate(new AuthLoginValidatorDto(user, serviceInDto.getPassword()));
		} catch (UnauthorizedException e) {
			handleFailure(user);
			throw e;
		}

		// 成功したら、失敗の回数を0に戻す（0のときは書き換えない）
		if (user.getFailedLoginCount() != null && user.getFailedLoginCount() > 0) {
			failureRecorder.resetFailures(user.getId());
		}

		log.info("ログインしました（userId={}）", user.getId());
		return AuthLoginRestOutDto.from(loginUserInfoAssembler.assemble(user));
	}

	/**
	 * ログインできなかったとき、パスワードの誤りだけを数える（詳細設計 4.1）。
	 * ユーザがいて、パスワードがあり、ロックされていないなら、validate が投げる理由は、パスワードの誤りだけ。
	 * 登録のないメールアドレス・パスワードがまだない・ロック中は、数えない（画面設計 2.1 の5）。
	 */
	private void handleFailure(User user) {
		if (user == null || user.getPasswordHash() == null || user.getLockedAt() != null) {
			return;
		}
		Optional<IssuedPasswordLink> link = failureRecorder.recordPasswordFailure(user.getId());
		// 確定したあとで送る（Slack は @Transactional の外。CLAUDE.md 5章）
		link.ifPresent(issued -> lockNotifier.notifyLocked(user, issued));
	}
}
