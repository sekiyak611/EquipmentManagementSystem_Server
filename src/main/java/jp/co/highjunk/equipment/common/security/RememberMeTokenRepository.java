package jp.co.highjunk.equipment.common.security;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.dao.entity.PersistentLogin;
import jp.co.highjunk.equipment.dao.entity.PersistentLoginExample;
import jp.co.highjunk.equipment.dao.mapper.PersistentLoginMapper;
import lombok.RequiredArgsConstructor;

/**
 * 「ログインしたままにする」の合言葉を、データベース（persistent_logins。データ設計 5.9）に持つ部品
 * （Spring Security の PersistentTokenRepository の中身。詳細設計 common.md 3.17）。
 * MyBatis の PersistentLoginMapper で読み書きする。ログアウトで、この端末の分だけを消すため、番号を指定して消す removeSeries を足した。
 */
@Component
@RequiredArgsConstructor
public class RememberMeTokenRepository implements PersistentTokenRepository {

	private final PersistentLoginMapper persistentLoginMapper;

	private final Clock clock;

	/** この端末の「ログインしたまま」を、新しく作る。 */
	@Override
	public void createNewToken(PersistentRememberMeToken token) {
		PersistentLogin row = new PersistentLogin();
		row.setUsername(token.getUsername());
		row.setSeries(token.getSeries());
		row.setToken(token.getTokenValue());
		row.setLastUsed(toLocalDateTime(token.getDate()));
		persistentLoginMapper.insert(row);
	}

	/** 自動ログインのたびに、合言葉と最後に使った日時を新しくする。 */
	@Override
	public void updateToken(String series, String tokenValue, Date lastUsed) {
		PersistentLogin row = new PersistentLogin();
		row.setSeries(series);
		row.setToken(tokenValue);
		row.setLastUsed(toLocalDateTime(lastUsed));
		persistentLoginMapper.updateByPrimaryKeySelective(row);
	}

	/** 番号の行を読む。なければ null。 */
	@Override
	public PersistentRememberMeToken getTokenForSeries(String seriesId) {
		PersistentLogin row = persistentLoginMapper.selectByPrimaryKey(seriesId);
		if (row == null) {
			return null;
		}
		return new PersistentRememberMeToken(row.getUsername(), row.getSeries(), row.getToken(),
				toDate(row.getLastUsed()));
	}

	/** その人の行を、すべて消す（パスワードを変えた・再設定したとき。盗まれたと見たとき）。 */
	@Override
	public void removeUserTokens(String username) {
		PersistentLoginExample example = new PersistentLoginExample();
		example.createCriteria().andUsernameEqualTo(username);
		persistentLoginMapper.deleteByExample(example);
	}

	/** 番号の行を1つだけ消す（ログアウトしたとき。ほかの端末の分は残る）。 */
	public void removeSeries(String series) {
		persistentLoginMapper.deleteByPrimaryKey(series);
	}

	private LocalDateTime toLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), clock.getZone());
	}

	private Date toDate(LocalDateTime dateTime) {
		return Date.from(dateTime.atZone(clock.getZone()).toInstant());
	}
}
