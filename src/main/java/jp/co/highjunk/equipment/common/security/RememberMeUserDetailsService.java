package jp.co.highjunk.equipment.common.security;

import java.util.List;
import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.dao.entity.User;
import jp.co.highjunk.equipment.dao.entity.UserExample;
import jp.co.highjunk.equipment.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

/**
 * 「ログインしたままにする」のクッキーのユーザ名（メールアドレス）から、ユーザを探す部品（詳細設計 common.md 3.17）。
 * 削除されたユーザは、見つからない扱い（自動ログインしない。クッキーを消す）。
 * 返す UserDetails は、Spring Security の仕組みが、ユーザ名を取り出すためだけのもの（パスワード・権限は入れない。
 * 権限項目は、あとの LoginRefreshFilter が読み直す）。
 */
@Component
@RequiredArgsConstructor
public class RememberMeUserDetailsService implements UserDetailsService {

	private final UserMapper userMapper;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = findUser(username);
		if (user == null) {
			throw new UsernameNotFoundException("ユーザが見つかりません");
		}
		return org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password("")
				.authorities(List.of()).build();
	}

	/** メールアドレス（小文字にそろえて保存している）で、削除されていないユーザを探す。いなければ null。 */
	User findUser(String email) {
		UserExample example = new UserExample();
		example.createCriteria().andEmailEqualTo(email.toLowerCase(Locale.ROOT)).andDeletedAtIsNull();
		List<User> users = userMapper.selectByExample(example);
		return users.isEmpty() ? null : users.get(0);
	}
}
