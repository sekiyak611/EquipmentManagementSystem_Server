package jp.co.highjunk.equipment.auth.validator;

import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.auth.model.User;
import jp.co.highjunk.equipment.common.error.UnauthorizedException;
import jp.co.highjunk.equipment.mapper.auth.AuthMapper;

/**
 * A-01 の業務チェック：メールアドレスとパスワードを確かめる（詳細設計 auth/A-01_ログイン.md 4・5章）。
 * 読むだけで、書き換えない（CLAUDE.md 5章）。
 */
@Component
public class LoginValidator {

	/** ログインできないときの文。理由にかかわらず同じ（画面設計 2章、docs/API設計.md 4.1） */
	public static final String LOGIN_FAILED = "メールアドレスまたはパスワードが違います";

	private static final Logger log = LoggerFactory.getLogger(LoginValidator.class);

	private final AuthMapper authMapper;

	private final PasswordEncoder passwordEncoder;

	/**
	 * ユーザがいない・パスワードがまだないときに照らし合わせる、仮のハッシュ。
	 * 照らし合わせ（bcrypt）は時間がかかるので、しないと返事の速さで登録の有無が分かってしまうため。
	 */
	private final String dummyHash;

	public LoginValidator(AuthMapper authMapper, PasswordEncoder passwordEncoder) {
		this.authMapper = authMapper;
		this.passwordEncoder = passwordEncoder;
		this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
	}

	/**
	 * メールアドレスとパスワードが正しく、ログインできる人かを確かめる。
	 *
	 * @return ログインできる人
	 * @throws UnauthorizedException 登録がない・削除された、パスワードがまだない・違う、ロック中のとき（どれも同じ文）
	 */
	public User validate(String email, String password) {
		User user = authMapper.selectUserByEmail(email.toLowerCase(Locale.ROOT));
		if (user == null || user.getPasswordHash() == null) {
			passwordEncoder.matches(password, dummyHash);
			log.warn("ログインできませんでした（{}）", user == null ? "登録のないメールアドレス" : "パスワードがまだない userId=" + user.getId());
			throw new UnauthorizedException(LOGIN_FAILED);
		}
		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			log.warn("ログインできませんでした（パスワードの誤り userId={}）", user.getId());
			throw new UnauthorizedException(LOGIN_FAILED);
		}
		if (user.getLockedAt() != null) {
			log.warn("ログインできませんでした（ロック中 userId={}）", user.getId());
			throw new UnauthorizedException(LOGIN_FAILED);
		}
		return user;
	}
}
