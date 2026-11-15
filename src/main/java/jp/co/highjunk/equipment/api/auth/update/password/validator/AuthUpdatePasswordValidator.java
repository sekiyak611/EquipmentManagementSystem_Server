package jp.co.highjunk.equipment.api.auth.update.password.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.api.auth.update.password.service.AuthUpdatePasswordWrongCurrentException;
import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.common.security.PasswordBlocklist;
import lombok.RequiredArgsConstructor;

/**
 * A-07 の業務チェック：現在のパスワードが合うか、新しいパスワードが使えるか（詳細設計 auth/A-07_自分のパスワード変更.md の4章の4・5章）。
 * 読んだユーザのハッシュ・メールアドレスが要るので、RestInDto ではなく Validator で確かめる（CLAUDE.md 5章）。読むだけ。
 * 回数を数える（書き換える）のは、ここではなく ServiceImpl が行う。
 */
@Component
@RequiredArgsConstructor
public class AuthUpdatePasswordValidator {

	/** 新しいパスワードが、現在のパスワードと同じときの文（画面設計 7章） */
	public static final String SAME_AS_CURRENT = "現在と同じパスワードは使えません";

	/** 新しいパスワードが、使えないものと同じときの文（画面設計 8章） */
	public static final String BLOCKED = "このパスワードはよく使われているため、使えません。別のパスワードにしてください";

	private static final Logger log = LoggerFactory.getLogger(AuthUpdatePasswordValidator.class);

	private final PasswordEncoder passwordEncoder;

	private final PasswordBlocklist passwordBlocklist;

	/**
	 * 確かめる順番：現在のパスワード → 新しいパスワードが現在と同じでないか → 使えないものと同じでないか。
	 *
	 * @throws AuthUpdatePasswordWrongCurrentException 現在のパスワードが合わないとき（入力欄 currentPassword。400）
	 * @throws FieldErrorException                     新しいパスワードが使えないとき（入力欄 password。400）
	 */
	public void validate(AuthUpdatePasswordValidatorDto validatorDto) {
		String hash = validatorDto.getPasswordHash();
		if (hash == null || !passwordEncoder.matches(validatorDto.getCurrentPassword(), hash)) {
			log.warn("パスワードを変更できませんでした（現在のパスワードが違う）");
			throw new AuthUpdatePasswordWrongCurrentException();
		}
		// パスワードもメールアドレスも出さない
		if (validatorDto.getPassword().equals(validatorDto.getCurrentPassword())) {
			throw new FieldErrorException("password", SAME_AS_CURRENT);
		}
		if (passwordBlocklist.isBlocked(validatorDto.getPassword(), validatorDto.getEmail())) {
			log.info("パスワードの変更を断りました（使えないパスワード）");
			throw new FieldErrorException("password", BLOCKED);
		}
	}
}
