package jp.co.highjunk.equipment.category;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.security.LoginUser;

/**
 * category 機能の API のテストで共通に使う値と道具（CategoryCreateApiTest・CategoryUpdateApiTest）。
 * ログインは、開発用のユーザ（common.md 4章）の LoginUser を入れて行う。権限項目は、APIを呼ぶたびにデータベースから読み直される
 * （総務 soumu@example.com は CATEGORY_MANAGE を持ち、一般社員 ippan@example.com は持たない）。
 * 最初のデータ（V2）に、種別が6つ（1 A：PC、2 B：ヘッドセット、3 C：マウス、4 D：カメラ、5 E：モニター、6 F：アダプタ）ある。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional。docs/テスト方針.md 4章）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class CategoryApiTestBase {

	static final String SOUMU = "soumu@example.com";

	static final String IPPAN = "ippan@example.com";

	/** 最初のデータの PC（頭文字 A）の ID */
	static final long PC_ID = 1;

	/** 最初のデータのマウス（頭文字 C）の ID */
	static final long MOUSE_ID = 3;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	/** 開発用のユーザでログインした状態にする。 */
	RequestPostProcessor login(String email) {
		LoginUser loginUser = new LoginUser(userId(email), LocalDateTime.now());
		return authentication(new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
	}

	long userId(String email) {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
	}

	int countCategories() {
		return jdbc.queryForObject("SELECT COUNT(*) FROM categories", Integer.class);
	}
}
