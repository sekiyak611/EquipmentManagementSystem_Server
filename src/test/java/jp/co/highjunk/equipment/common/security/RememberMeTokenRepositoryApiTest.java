package jp.co.highjunk.equipment.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;

/**
 * 「ログインしたままにする」の合言葉を、データベース（persistent_logins）に持つ部品（詳細設計 common.md 3.17）のテスト。
 * データベースを使う（テストのあとに元に戻す）。
 */
@DisplayName("RememberMeTokenRepository")
class RememberMeTokenRepositoryApiTest extends AuthApiTestBase {

	@Autowired
	RememberMeTokenRepository repository;

	private PersistentRememberMeToken token(String username, String series, String value) {
		return new PersistentRememberMeToken(username, series, value, new Date());
	}

	@Test
	@DisplayName("作った行を、番号で読める（ユーザ名・合言葉・最後に使った日時）。ない番号は null")
	void createAndRead() {
		Date now = new Date();
		repository.createNewToken(new PersistentRememberMeToken(IPPAN, "series-a", "token-a", now));

		PersistentRememberMeToken found = repository.getTokenForSeries("series-a");

		assertThat(found.getUsername()).isEqualTo(IPPAN);
		assertThat(found.getTokenValue()).isEqualTo("token-a");
		// データベースは秒まで。同じ秒の中に収まる
		assertThat(Math.abs(found.getDate().getTime() - now.getTime())).isLessThan(1000);
		assertThat(repository.getTokenForSeries("no-such-series")).isNull();
	}

	@Test
	@DisplayName("合言葉と最後に使った日時を新しくできる。ほかの列（ユーザ名）は変わらない")
	void update() {
		repository.createNewToken(token(IPPAN, "series-a", "token-a"));
		Date later = new Date(System.currentTimeMillis() + 60_000);

		repository.updateToken("series-a", "token-b", later);

		PersistentRememberMeToken found = repository.getTokenForSeries("series-a");
		assertThat(found.getTokenValue()).isEqualTo("token-b");
		assertThat(found.getUsername()).isEqualTo(IPPAN);
		assertThat(Math.abs(found.getDate().getTime() - later.getTime())).isLessThan(1000);
	}

	@Test
	@DisplayName("removeSeries：その番号の行だけが消える（同じ人の、ほかの端末の行は残る）")
	void removeSeries() {
		repository.createNewToken(token(IPPAN, "series-a", "token-a"));
		repository.createNewToken(token(IPPAN, "series-b", "token-b"));

		repository.removeSeries("series-a");

		assertThat(repository.getTokenForSeries("series-a")).isNull();
		assertThat(repository.getTokenForSeries("series-b")).isNotNull();
	}

	@Test
	@DisplayName("removeUserTokens：その人の行がすべて消える（ほかの人の行は残る）")
	void removeUserTokens() {
		repository.createNewToken(token(IPPAN, "series-a", "token-a"));
		repository.createNewToken(token(IPPAN, "series-b", "token-b"));
		repository.createNewToken(token(SOUMU, "series-c", "token-c"));

		repository.removeUserTokens(IPPAN);

		assertThat(repository.getTokenForSeries("series-a")).isNull();
		assertThat(repository.getTokenForSeries("series-b")).isNull();
		assertThat(repository.getTokenForSeries("series-c")).isNotNull();
	}

	@Test
	@DisplayName("ない番号を消しても、何も起きない")
	void removeUnknown() {
		repository.removeSeries("no-such-series");
		repository.removeUserTokens("nobody@example.com");
	}
}
