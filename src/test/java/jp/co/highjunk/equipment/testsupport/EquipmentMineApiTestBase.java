package jp.co.highjunk.equipment.testsupport;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
 * api.equipment.mine の API のテストで共通に使う値と道具（B-01・B-03 のテストが継ぐ。B-04 なども継ぐ）。
 * API ごとにパッケージが分かれるので、テストで共通に使う値と道具は testsupport に置く（CLAUDE.md 5章。2026-11-15 に決めた）。
 * ログインは、開発用のユーザ（common.md 4章）の LoginUser を入れて行う。一般社員 ippan@example.com・総務 soumu@example.com は、
 * どちらも LOAN_SELF を持つ。最初のデータ（V2）に、種別が6つ（1 A：PC、2 B：ヘッドセット、…）ある。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional。docs/テスト方針.md 4章）。今日は、アプリの時計（Clock）の今日。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class EquipmentMineApiTestBase {

	public static final String SOUMU = "soumu@example.com";

	public static final String IPPAN = "ippan@example.com";

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected JdbcTemplate jdbc;

	@Autowired
	protected Clock clock;

	/**
	 * 日付が変わる直前（深夜0時の5秒前から）なら、0時を過ぎるまで待つ。テストの中で今日の日付を取ってから、
	 * サーバーが自分で今日を取るまでの間に、日付が変わると、期待する日付が食い違うため（テストの @BeforeEach で呼ぶ）。
	 */
	protected void waitIfNearMidnight() throws InterruptedException {
		LocalTime now = LocalTime.now(clock);
		if (now.isAfter(LocalTime.of(23, 59, 55))) {
			Thread.sleep(java.time.Duration.between(now, LocalTime.MAX).toMillis() + 1500);
		}
	}

	protected RequestPostProcessor login(String email) {
		return loginAs(userId(email));
	}

	protected RequestPostProcessor loginAs(long userId) {
		return authentication(new UsernamePasswordAuthenticationToken(new LoginUser(userId, LocalDateTime.now()),
				null, List.of()));
	}

	protected long userId(String email) {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
	}

	/** 権限項目を1つも持たない権限（ロール）を作り、ID を返す */
	protected long insertRoleWithoutPermission() {
		jdbc.update("INSERT INTO roles (name, is_default, created_at, updated_at) VALUES ('権限なし', FALSE, NOW(), NOW())");
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	protected long insertUser(String email, long roleId) {
		jdbc.update("INSERT INTO users (name, email, role_id, created_at, updated_at) VALUES ('権限なしの人', ?, ?, NOW(), NOW())",
				email, roleId);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	/** 備品を1つ入れ、ID を返す（種別は ID で決める。1＝PC、2＝ヘッドセット） */
	protected long equipment(String managementNo, String status, long categoryId, String maker, String model,
			LocalDate lastConfirmedOn) {
		jdbc.update("INSERT INTO equipment (management_no, category_id, maker, model, status, last_confirmed_on,"
				+ " created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())", managementNo, categoryId, maker,
				model, status, lastConfirmedOn);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	/** 貸出履歴を1つ入れ、ID を返す（返却日があれば返却済み） */
	protected long loan(long equipmentId, long userId, LocalDate lentOn, LocalDate dueOn, LocalDate returnedOn) {
		return loan(equipmentId, userId, lentOn, dueOn, returnedOn, null);
	}

	/** 貸出履歴を1つ入れ、ID を返す（返却日があれば返却済み。メモは貸出のときのメモ） */
	protected long loan(long equipmentId, long userId, LocalDate lentOn, LocalDate dueOn, LocalDate returnedOn,
			String memo) {
		jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, due_on, returned_on, memo, created_at,"
				+ " updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())", equipmentId, userId, lentOn, dueOn,
				returnedOn, memo);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}
}
