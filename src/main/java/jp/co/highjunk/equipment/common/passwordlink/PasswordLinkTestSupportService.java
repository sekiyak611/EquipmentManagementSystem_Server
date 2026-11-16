package jp.co.highjunk.equipment.common.passwordlink;

import java.util.List;
import java.util.Locale;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.constant.PasswordLinkPurpose;
import jp.co.highjunk.equipment.common.error.NotFoundException;
import jp.co.highjunk.equipment.dao.entity.PasswordTokenExample;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.entity.UserExample;
import jp.co.highjunk.equipment.dao.mapper.PasswordTokenMapper;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

/**
 * テスト用：パスワード設定用のリンクを、決まった用途で作る・全部消す（詳細設計 common.md 3.14）。
 * 画面のテスト（Playwright）で、5分に1回の決まり（A-04）や DM を経由せずに、S-03 などを確かめるために使う。
 * app.test-support.enabled が true のときだけ動く。本番・開発では、この Bean も controller もなく、404 になる。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.test-support", name = "enabled", havingValue = "true")
public class PasswordLinkTestSupportService {

	private final PasswordLinkIssuer passwordLinkIssuer;

	private final PasswordTokenMapper passwordTokenMapper;

	private final UserMapper userMapper;

	/**
	 * そのメールアドレスのユーザに、リンクを作る（前のリンクは無効になる）。
	 *
	 * @return 作ったリンクの合言葉と、用途・有効期限
	 * @throws NotFoundException そのメールアドレスのユーザがいないとき（削除されたユーザも）
	 */
	@Transactional
	public PasswordLinkTestSupportIssued issue(String email, PasswordLinkPurpose purpose) {
		UserExample example = new UserExample();
		example.createCriteria().andEmailEqualTo(email.toLowerCase(Locale.ROOT)).andDeletedAtIsNull();
		List<User> users = userMapper.selectByExample(example);
		if (users.isEmpty()) {
			throw new NotFoundException();
		}
		IssuedPasswordLink link = passwordLinkIssuer.issue(users.get(0).getId(), purpose);
		String url = link.getLinkUrl();
		String token = url.substring(url.indexOf("token=") + "token=".length());
		return new PasswordLinkTestSupportIssued(token, link.getPurpose().name(), link.getExpiresAt().toString());
	}

	/** パスワード設定用のリンクを、全部消す（5分に1回の決まりに当たらないよう、テストの前に使う）。 */
	@Transactional
	public void clear() {
		passwordTokenMapper.deleteByExample(new PasswordTokenExample());
	}
}
