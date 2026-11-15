package jp.co.highjunk.equipment.common.security;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.config.AppProperties;
import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.common.passwordlink.IssuedPasswordLink;
import jp.co.highjunk.equipment.common.passwordlink.PasswordLinkIssuer;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.entity.UserExample;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.UserCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * パスワードを間違えた回数を数える・ロックする・0に戻す（詳細設計 common.md 3.16、auth/A-01_ログイン.md 4.1）。
 * A-01（ログイン）と A-07（自分のパスワード変更）が使う（ZPOP-61 で、api.auth.login.service から common.security に移した）。
 * ServiceImpl から分けた理由：失敗は例外で終わるので、ServiceImpl の {@code @Transactional} の中だと、
 * 数えた回数も一緒に元に戻ってしまう。ここは別のクラスなので、自分のまとまりを確定してから、ServiceImpl が例外を投げられる。
 */
@Component
@RequiredArgsConstructor
public class LoginFailureRecorder {

	private static final Logger log = LoggerFactory.getLogger(LoginFailureRecorder.class);

	private final UserMapper userMapper;

	private final UserCustomMapper userCustomMapper;

	private final PasswordLinkIssuer passwordLinkIssuer;

	private final AppProperties appProperties;

	private final Clock clock;

	/**
	 * パスワードを間違えたことを数える。しきい値に届いたらロックして、パスワード設定用のリンク（LOCK）を作る。
	 *
	 * @return いま、ロックしたときだけ、作ったリンク（DMを送る側が使う）。数えただけ・もう数えられない（同時にロックされた）ときは空
	 */
	@Transactional
	public Optional<IssuedPasswordLink> recordPasswordFailure(long userId) {
		LocalDateTime now = LocalDateTime.now(clock);
		if (userCustomMapper.incrementFailedLoginCount(userId, now) == 0) {
			return Optional.empty();
		}
		User user = userMapper.selectByPrimaryKey(userId);
		if (user == null || user.getFailedLoginCount() < appProperties.lockThreshold()) {
			return Optional.empty();
		}

		// locked_at が空の行だけを書き換える。同時に何回も間違えられても、ロックする（=DMを送る）のは1回だけ
		User locked = new User();
		locked.setLockedAt(now);
		locked.setUpdatedAt(now);
		UserExample example = new UserExample();
		example.createCriteria().andIdEqualTo(userId).andLockedAtIsNull();
		if (userMapper.updateByExampleSelective(locked, example) == 0) {
			return Optional.empty();
		}
		log.warn("アカウントをロックしました（userId={}、失敗の回数={}）", userId, user.getFailedLoginCount());
		return Optional.of(passwordLinkIssuer.issue(userId, PasswordLinkPurpose.LOCK));
	}

	/** ログインに成功したので、失敗の回数を0に戻す。 */
	@Transactional
	public void resetFailures(long userId) {
		User reset = new User();
		reset.setFailedLoginCount(0);
		reset.setUpdatedAt(LocalDateTime.now(clock));
		UserExample example = new UserExample();
		example.createCriteria().andIdEqualTo(userId);
		userMapper.updateByExampleSelective(reset, example);
	}
}
