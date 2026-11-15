package jp.co.highjunk.equipment.api.auth.passwordtokens.use.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.common.security.PasswordBlocklist;
import lombok.RequiredArgsConstructor;

/**
 * A-06 の業務チェック：新しいパスワードが、使えないものと同じでないかを確かめる（詳細設計 auth/A-06_合言葉でパスワードを決める.md の5章 No.5）。
 * 読んだユーザのメールアドレスが要るので、RestInDto ではなく Validator で確かめる（CLAUDE.md 5章）。読むだけ。
 */
@Component
@RequiredArgsConstructor
public class AuthPasswordTokensUseValidator {

	/** 使えないときの文（画面設計 8章） */
	public static final String BLOCKED = "このパスワードはよく使われているため、使えません。別のパスワードにしてください";

	private static final Logger log = LoggerFactory.getLogger(AuthPasswordTokensUseValidator.class);

	private final PasswordBlocklist passwordBlocklist;

	/**
	 * パスワードが、メールアドレス・システム名・よく使われるパスワードと同じなら断る。
	 *
	 * @throws FieldErrorException 同じとき（入力欄 password のエラー。400）
	 */
	public void validate(AuthPasswordTokensUseValidatorDto validatorDto) {
		if (passwordBlocklist.isBlocked(validatorDto.getPassword(), validatorDto.getEmail())) {
			// パスワードもメールアドレスも出さない
			log.info("パスワードの設定を断りました（使えないパスワード）");
			throw new FieldErrorException("password", BLOCKED);
		}
	}
}
