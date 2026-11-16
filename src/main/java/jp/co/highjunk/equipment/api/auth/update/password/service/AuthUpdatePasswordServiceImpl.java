package jp.co.highjunk.equipment.api.auth.update.password.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.api.auth.update.password.validator.AuthUpdatePasswordValidator;
import jp.co.highjunk.equipment.api.auth.update.password.validator.AuthUpdatePasswordValidatorDto;
import jp.co.highjunk.equipment.common.error.ApiMessages;
import jp.co.highjunk.equipment.common.error.UnauthorizedException;
import jp.co.highjunk.equipment.common.passwordlink.IssuedPasswordLink;
import jp.co.highjunk.equipment.common.passwordlink.PasswordChangedNotifier;
import jp.co.highjunk.equipment.common.security.AccountLockNotifier;
import jp.co.highjunk.equipment.common.security.LoginFailureRecorder;
import jp.co.highjunk.equipment.common.security.LoginRememberMe;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.UserCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * A-07 自分のパスワード変更の業務の処理の中身（詳細設計 auth/A-07_自分のパスワード変更.md の4章）。
 * このメソッドには @Transactional を付けない。users を書き換えるのは1つの UPDATE だけで、Slack の送信を、
 * 書き込みが確定したあとにするため。回数を数える処理は、別のクラス LoginFailureRecorder が、自分のまとまりで行う。
 */
@Service
@RequiredArgsConstructor
public class AuthUpdatePasswordServiceImpl implements AuthUpdatePasswordService {

	private static final Logger log = LoggerFactory.getLogger(AuthUpdatePasswordServiceImpl.class);

	private final UserMapper userMapper;

	private final UserCustomMapper userCustomMapper;

	private final AuthUpdatePasswordValidator validator;

	private final LoginFailureRecorder failureRecorder;

	private final AccountLockNotifier lockNotifier;

	private final PasswordChangedNotifier changedNotifier;

	private final LoginRememberMe loginRememberMe;

	private final PasswordEncoder passwordEncoder;

	private final Clock clock;

	@Override
	public void update(AuthUpdatePasswordServiceInDto serviceInDto) {
		User user = userMapper.selectByPrimaryKey(serviceInDto.getUserId());
		if (user == null || user.getDeletedAt() != null) {
			// ログイン状態の読み直しで、削除されたユーザはログアウトになっているので、ふつうは来ない
			throw new UnauthorizedException(ApiMessages.UNAUTHORIZED);
		}

		try {
			validator.validate(new AuthUpdatePasswordValidatorDto(user.getEmail(), user.getPasswordHash(),
					serviceInDto.getCurrentPassword(), serviceInDto.getPassword()));
		} catch (AuthUpdatePasswordWrongCurrentException e) {
			// 現在のパスワードの間違いは、ログインの失敗と同じく数える。ロック中は数えない（SQL の条件）
			Optional<IssuedPasswordLink> link = failureRecorder.recordPasswordFailure(user.getId());
			if (link.isPresent()) {
				// 確定したあとで送る（Slack は @Transactional の外。CLAUDE.md 5章）
				lockNotifier.notifyLocked(user, link.get());
				throw new AuthUpdatePasswordAccountLockedException();
			}
			throw e;
		}

		LocalDateTime now = LocalDateTime.now(clock);
		// session_valid_after は秒までにそろえて入れる（A-06 の12章 No.1）。読んだあとに変わっていたら、書き換えない
		int updated = userCustomMapper.changePassword(user.getId(), user.getPasswordHash(),
				passwordEncoder.encode(serviceInDto.getPassword()), now.truncatedTo(ChronoUnit.SECONDS), now);
		if (updated != 1) {
			log.warn("パスワードを変更できませんでした（読んだあとにパスワードが変わった userId={}）", user.getId());
			throw new AuthUpdatePasswordWrongCurrentException();
		}
		// すべての端末の「ログインしたまま」を切る。この端末の分は、Controller が作り直す（common.md 3.17の6）
		loginRememberMe.removeAllOf(user.getEmail());
		log.info("パスワードを変更しました（userId={}）", user.getId());

		changedNotifier.notifyChanged(user.getEmail(), user.getId(), now);
	}
}
