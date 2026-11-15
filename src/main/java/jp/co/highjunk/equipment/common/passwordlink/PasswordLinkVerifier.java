package jp.co.highjunk.equipment.common.passwordlink;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.dao.entity.PasswordToken;
import jp.co.highjunk.equipment.dao.entity.PasswordTokenExample;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.mapper.PasswordTokenMapper;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

/**
 * パスワード設定用のリンクの合言葉が使えるかを確かめる（詳細設計 common.md 3.14。A-05・A-06 が使う）。
 * 読むだけ。自分では {@code @Transactional} を付けない（A-06 は、自分のまとまりの中で呼ぶ）。
 */
@Component
@RequiredArgsConstructor
public class PasswordLinkVerifier {

	/** 使えないときの文。理由にかかわらず同じ（画面設計 6章、API設計 4.4） */
	public static final String UNUSABLE_MESSAGE = "このリンクは使えません。有効期限が切れたか、すでに使われています。";

	private static final Logger log = LoggerFactory.getLogger(PasswordLinkVerifier.class);

	private final PasswordTokenMapper passwordTokenMapper;

	private final UserMapper userMapper;

	private final Clock clock;

	/**
	 * 合言葉が使えるかを確かめる。
	 *
	 * @param token リンクの合言葉（ログに出さない）
	 * @return 使えるときの、リンクの行・ユーザ・用途
	 * @throws BusinessRuleException 使えないとき（ない・使用済み・無効・期限切れ・削除されたユーザ。どれも同じ文。400）
	 */
	public VerifiedPasswordLink verify(String token) {
		if (token == null || token.isBlank()) {
			throw unusable("合言葉が空", null);
		}

		PasswordTokenExample example = new PasswordTokenExample();
		example.createCriteria().andTokenHashEqualTo(PasswordLinkTokens.hash(token));
		List<PasswordToken> rows = passwordTokenMapper.selectByExample(example);
		if (rows.isEmpty()) {
			throw unusable("合言葉がデータベースにない", null);
		}
		PasswordToken row = rows.get(0);
		Long userId = row.getUserId();

		if (row.getUsedAt() != null) {
			throw unusable("使用済み", userId);
		}
		if (row.getRevokedAt() != null) {
			throw unusable("無効（より新しいリンクがある）", userId);
		}
		if (!row.getExpiresAt().isAfter(LocalDateTime.now(clock))) {
			throw unusable("期限切れ", userId);
		}

		User user = userMapper.selectByPrimaryKey(userId);
		if (user == null || user.getDeletedAt() != null) {
			throw unusable("削除されたユーザ", userId);
		}
		return new VerifiedPasswordLink(row, user, PasswordLinkPurpose.valueOf(row.getPurpose()));
	}

	/** 理由をログに残して、同じ文の例外を作る（合言葉は出さない）。 */
	private BusinessRuleException unusable(String reason, Long userId) {
		log.warn("パスワード設定用のリンクが使えません（理由={}、userId={}）", reason, userId);
		return new BusinessRuleException(UNUSABLE_MESSAGE);
	}
}
