package jp.co.highjunk.equipment.testsupport;

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
 * api.equipment.category の API のテストで共通に使う値と道具（K-02〜K-04 のテストが継ぐ）。
 * API ごとにパッケージが分かれるので、テストで共通に使う値と道具は testsupport に置く（CLAUDE.md 5章。2026-11-15 に決めた）。
 * ログインは、開発用のユーザ（common.md 4章）の LoginUser を入れて行う。権限項目は、APIを呼ぶたびにデータベースから読み直される
 * （総務 soumu@example.com は CATEGORY_MANAGE を持ち、一般社員 ippan@example.com は持たない）。
 * 最初のデータ（V2）に、種別が6つ（1 A：PC、2 B：ヘッドセット、3 C：マウス、4 D：カメラ、5 E：モニター、6 F：アダプタ）ある。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional。docs/テスト方針.md 4章）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class EquipmentCategoryApiTestBase {

	public static final String SOUMU = "soumu@example.com";

	public static final String IPPAN = "ippan@example.com";

	/** 最初のデータの PC（頭文字 A）の ID */
	public static final long PC_ID = 1;

	/** 最初のデータのマウス（頭文字 C）の ID */
	public static final long MOUSE_ID = 3;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected JdbcTemplate jdbc;

	/** 開発用のユーザでログインした状態にする。 */
	protected RequestPostProcessor login(String email) {
		LoginUser loginUser = new LoginUser(userId(email), LocalDateTime.now());
		return authentication(new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
	}

	protected long userId(String email) {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
	}

	protected int countCategories() {
		return jdbc.queryForObject("SELECT COUNT(*) FROM categories", Integer.class);
	}
}
