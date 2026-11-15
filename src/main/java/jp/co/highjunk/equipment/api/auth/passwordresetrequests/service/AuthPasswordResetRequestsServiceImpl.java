package jp.co.highjunk.equipment.api.auth.passwordresetrequests.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.common.passwordlink.IssuedPasswordLink;
import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.entity.UserExample;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

/**
 * A-04 パスワード設定用のリンクの依頼の業務の処理の中身（詳細設計 auth/A-04_パスワード設定リンクの依頼.md の4章）。
 * このメソッドには @Transactional を付けない。Slack の送信を、リンクを作る書き込みが確定したあとにするため
 * （書き込みは AuthPasswordResetRequestsLinkIssuer が、自分のまとまりで行う）。
 * どの場合も例外にしない。登録の有無などを、返す値から分からなくするため（画面設計 5章）。
 */
@Service
@RequiredArgsConstructor
public class AuthPasswordResetRequestsServiceImpl implements AuthPasswordResetRequestsService {

	private static final Logger log = LoggerFactory.getLogger(AuthPasswordResetRequestsServiceImpl.class);

	private final UserMapper userMapper;

	private final AuthPasswordResetRequestsLinkIssuer linkIssuer;

	private final AuthPasswordResetRequestsNotifier notifier;

	@Override
	public void request(AuthPasswordResetRequestsServiceInDto serviceInDto) {
		// メールアドレスは小文字にそろえて保存している（データ設計 2章）。削除されたユーザには送らない
		UserExample example = new UserExample();
		example.createCriteria().andEmailEqualTo(serviceInDto.getEmail().toLowerCase(Locale.ROOT))
				.andDeletedAtIsNull();
		List<User> users = userMapper.selectByExample(example);
		if (users.isEmpty()) {
			// メールアドレスは出さない（登録の有無を、ログからも漏らさないため）
			log.info("パスワード再設定の依頼を受け付けましたが、登録のないメールアドレスなので、何もしません");
			return;
		}
		User user = users.get(0);

		Optional<IssuedPasswordLink> link = linkIssuer.issueIfAllowed(user.getId());
		// 確定したあとで送る（Slack は @Transactional の外。CLAUDE.md 5章）
		link.ifPresent(issued -> notifier.notifyRequested(user, issued));
	}
}
