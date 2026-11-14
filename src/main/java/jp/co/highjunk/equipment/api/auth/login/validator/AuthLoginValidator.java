package jp.co.highjunk.equipment.api.auth.login.validator;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.error.UnauthorizedException;
import jp.co.highjunk.equipment.dao.entity.User;

/**
 * A-01 の業務チェック：ログインできる人かを確かめる（詳細設計 auth/A-01_ログイン.md 4・5章）。
 * ServiceImpl が読んだユーザが要るので、ServiceInDto のチェックではなく Validator で確かめる（CLAUDE.md 5章）。読むだけ。
 */
@Component
public class AuthLoginValidator {

	/** ログインできないときの文。理由にかかわらず同じ（画面設計 2章、docs/API設計.md 4.1） */
	public static final String LOGIN_FAILED = "メールアドレスまたはパスワードが違います";

	private static final Logger log = LoggerFactory.getLogger(AuthLoginValidator.class);

	private final PasswordEncoder passwordEncoder;

	/**
	 * ユーザがいない・パスワードがまだないときに照らし合わせる、仮のハッシュ。
	 * 照らし合わせ（bcrypt）は時間がかかるので、しないと返事の速さで登録の有無が分かってしまうため。
	 */
	private final String dummyHash;

	public AuthLoginValidator(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
		this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
	}

	/**
	 * パスワードが正しく、ログインできる人かを確かめる。
	 *
	 * @throws UnauthorizedException 登録がない・削除された、パスワードがまだない・違う、ロック中のとき（どれも同じ文。401）
	 */
	public void validate(AuthLoginValidatorDto validatorDto) {
		User user = validatorDto.getUser();
		String password = validatorDto.getPassword();
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
	}
}
