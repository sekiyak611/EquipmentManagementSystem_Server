package jp.co.highjunk.equipment.api.equipment.management.search.item;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.security.LoginUser;

/**
 * C-01 備品を探す（docs/API設計.md 3.3・4.8、詳細設計 equipment/management/C-01_備品を探す.md）のテスト。
 * 備品・貸出履歴・ユーザは、テストごとに入れて、終わったら元に戻す（@Transactional。docs/テスト方針.md 4章）。
 * 「今月」は、アプリの時計（Clock）の今日から決める（テストを動かす月によらず同じ結果になるように）。
 * 最初のデータ（V2）の種別：1 PC、3 マウス。総務 soumu@example.com は EQUIPMENT_VIEW を持ち、一般社員 ippan@example.com は持たない。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("C-01 備品を探す")
class EquipmentManagementSearchItemApiTest {

	static final String SOUMU = "soumu@example.com";

	static final String IPPAN = "ippan@example.com";

	static final long PC = 1;

	static final long MOUSE = 3;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	Clock clock;

	/** 今月の1日・今日・先月の末日 */
	LocalDate firstDay;

	LocalDate today;

	LocalDate lastMonth;

	@BeforeEach
	void setUp() {
		today = LocalDate.now(clock);
		firstDay = today.withDayOfMonth(1);
		lastMonth = firstDay.minusDays(1);
	}

	@Test
	@DisplayName("ログインしていない：401")
	void notLoggedIn() throws Exception {
		mockMvc.perform(get("/api/equipment")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("EQUIPMENT_VIEW がない（一般社員）：403「この操作を行う権限がありません。」")
	void forbidden() throws Exception {
		search(IPPAN, get("/api/equipment"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("この操作を行う権限がありません。"));
	}

	@Test
	@DisplayName("備品がないとき：200。items は空、page 1・size 50・totalCount 0・totalPages 0")
	void empty() throws Exception {
		search(SOUMU, get("/api/equipment"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data.items", hasSize(0)))
				.andExpect(jsonPath("$.data.page").value(1))
				.andExpect(jsonPath("$.data.size").value(50))
				.andExpect(jsonPath("$.data.totalCount").value(0))
				.andExpect(jsonPath("$.data.totalPages").value(0))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@DisplayName("1件の項目：id・管理番号・種別の名前・メーカー・モデル・管理状態・貸出状態・借用者・最終確認日・今月確認済みか")
	void fields() throws Exception {
		long lent = equipment("A001", PC, "MANAGED", today, "Lenovo", "ThinkPad X1", null);
		loan(lent, user("山田太郎"), null, null);
		long stock = equipment("C001", MOUSE, "MANAGED", null, null, null, null);
		search(SOUMU, get("/api/equipment"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items", hasSize(2)))
				.andExpect(jsonPath("$.data.items[0].length()").value(10))
				.andExpect(jsonPath("$.data.items[0].id").value(lent))
				.andExpect(jsonPath("$.data.items[0].managementNo").value("A001"))
				.andExpect(jsonPath("$.data.items[0].categoryName").value("PC"))
				.andExpect(jsonPath("$.data.items[0].maker").value("Lenovo"))
				.andExpect(jsonPath("$.data.items[0].model").value("ThinkPad X1"))
				.andExpect(jsonPath("$.data.items[0].status").value("MANAGED"))
				.andExpect(jsonPath("$.data.items[0].loanStatus").value("LENT"))
				.andExpect(jsonPath("$.data.items[0].borrowerName").value("山田太郎"))
				.andExpect(jsonPath("$.data.items[0].lastConfirmedOn").value(today.toString()))
				.andExpect(jsonPath("$.data.items[0].confirmedThisMonth").value(true))
				// 保管中・未記入
				.andExpect(jsonPath("$.data.items[1].id").value(stock))
				.andExpect(jsonPath("$.data.items[1].categoryName").value("マウス"))
				.andExpect(jsonPath("$.data.items[1].maker").value(nullValue()))
				.andExpect(jsonPath("$.data.items[1].loanStatus").value("IN_STOCK"))
				.andExpect(jsonPath("$.data.items[1].borrowerName").value(nullValue()))
				.andExpect(jsonPath("$.data.items[1].lastConfirmedOn").value(nullValue()))
				.andExpect(jsonPath("$.data.items[1].confirmedThisMonth").value(false))
				.andExpect(jsonPath("$.data.totalCount").value(2))
				.andExpect(jsonPath("$.data.totalPages").value(1));
	}

	@Test
	@DisplayName("削除された備品は出さない。返却済み・取り消した貸出履歴だけの備品は保管中")
	void deletedAndReturned() throws Exception {
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, deleted_at, created_at, updated_at)"
				+ " VALUES ('X999', 1, 'MANAGED', NOW(), NOW(), NOW())");
		long returned = equipment("A001", PC, "MANAGED", null, null, null, null);
		loan(returned, user("返した人"), today, null);
		long canceled = equipment("A002", PC, "MANAGED", null, null, null, null);
		loan(canceled, user("取り消した人"), null, LocalDateTime.now());
		search(SOUMU, get("/api/equipment"))
				.andExpect(jsonPath("$.data.items[*].managementNo", contains("A001", "A002")))
				.andExpect(jsonPath("$.data.items[*].loanStatus", contains("IN_STOCK", "IN_STOCK")))
				.andExpect(jsonPath("$.data.items[*].borrowerName", contains(nullValue(), nullValue())));
	}

	@Test
	@DisplayName("最初の値の今月確認済みは「最終確認日の月が今月」：今月の1日は確認済み、先月の末日は未確認")
	void confirmedThisMonth() throws Exception {
		equipment("A001", PC, "MANAGED", firstDay, null, null, null);
		equipment("A002", PC, "MANAGED", lastMonth, null, null, null);
		search(SOUMU, get("/api/equipment"))
				.andExpect(jsonPath("$.data.items[*].confirmedThisMonth", contains(true, false)));
	}

	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@Transactional
	@DisplayName("並び順")
	class Sort {

		@BeforeEach
		void data() {
			equipment("B002", PC, "MANAGED", firstDay, null, null, null);
			equipment("A010", PC, "MANAGED", null, null, null, null);
			equipment("C001", PC, "MANAGED", lastMonth, null, null, null);
			equipment("A002", PC, "MANAGED", firstDay, null, null, null);
		}

		@Test
		@DisplayName("最初は管理番号の順")
		void defaultOrder() throws Exception {
			search(SOUMU, get("/api/equipment"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A002", "A010", "B002", "C001")));
		}

		@Test
		@DisplayName("sort=managementNo,desc：管理番号の逆順")
		void managementNoDesc() throws Exception {
			search(SOUMU, get("/api/equipment").param("sort", "managementNo,desc"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("C001", "B002", "A010", "A002")));
		}

		@Test
		@DisplayName("sort=lastConfirmedOn：古い順。未記入が先頭、同じ日は管理番号の順")
		void lastConfirmedOn() throws Exception {
			search(SOUMU, get("/api/equipment").param("sort", "lastConfirmedOn"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A010", "C001", "A002", "B002")));
		}

		@Test
		@DisplayName("sort=lastConfirmedOn,desc：新しい順。未記入が末尾、同じ日は管理番号の順")
		void lastConfirmedOnDesc() throws Exception {
			search(SOUMU, get("/api/equipment").param("sort", "lastConfirmedOn,desc"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A002", "B002", "C001", "A010")));
		}
	}

	@Test
	@DisplayName("50件ずつ：51件なら2ページ。2ページ目は1件、最後のページより後ろは空")
	void paging() throws Exception {
		for (int i = 1; i <= 51; i++) {
			equipment(String.format("A%03d", i), PC, "MANAGED", null, null, null, null);
		}
		search(SOUMU, get("/api/equipment"))
				.andExpect(jsonPath("$.data.items", hasSize(50)))
				.andExpect(jsonPath("$.data.items[0].managementNo").value("A001"))
				.andExpect(jsonPath("$.data.totalCount").value(51))
				.andExpect(jsonPath("$.data.totalPages").value(2));
		search(SOUMU, get("/api/equipment").param("page", "2"))
				.andExpect(jsonPath("$.data.items[*].managementNo", contains("A051")))
				.andExpect(jsonPath("$.data.page").value(2));
		search(SOUMU, get("/api/equipment").param("page", "3"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items", hasSize(0)))
				.andExpect(jsonPath("$.data.page").value(3))
				.andExpect(jsonPath("$.data.totalCount").value(51));
	}

	@Test
	@DisplayName("種別・管理状態（複数）で絞る")
	void categoryAndStatus() throws Exception {
		equipment("A001", PC, "MANAGED", null, null, null, null);
		equipment("A002", PC, "MISSING", null, null, null, null);
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, disposed_on, created_at, updated_at)"
				+ " VALUES ('A003', 1, 'DISPOSED', '2026-01-01', NOW(), NOW())");
		equipment("C001", MOUSE, "MANAGED", null, null, null, null);
		search(SOUMU, get("/api/equipment").param("categoryId", String.valueOf(PC)))
				.andExpect(jsonPath("$.data.items[*].managementNo", contains("A001", "A002", "A003")));
		search(SOUMU, get("/api/equipment").param("status", "MANAGED").param("status", "MISSING"))
				.andExpect(jsonPath("$.data.items[*].managementNo", contains("A001", "A002", "C001")));
		search(SOUMU, get("/api/equipment").param("status", "DISPOSED"))
				.andExpect(jsonPath("$.data.items[*].status", contains("DISPOSED")));
		// ない種別は誤りにせず、0件
		search(SOUMU, get("/api/equipment").param("categoryId", "999"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items", hasSize(0)));
	}

	@Test
	@DisplayName("貸出状態で絞る：LENT は貸出中だけ、IN_STOCK は保管中だけ、ALL はすべて")
	void loanStatus() throws Exception {
		long lent = equipment("A001", PC, "MANAGED", null, null, null, null);
		loan(lent, user("借りた人"), null, null);
		equipment("A002", PC, "MANAGED", null, null, null, null);
		search(SOUMU, get("/api/equipment").param("loanStatus", "LENT"))
				.andExpect(jsonPath("$.data.items[*].managementNo", contains("A001")));
		search(SOUMU, get("/api/equipment").param("loanStatus", "IN_STOCK"))
				.andExpect(jsonPath("$.data.items[*].managementNo", contains("A002")));
		search(SOUMU, get("/api/equipment").param("loanStatus", "ALL"))
				.andExpect(jsonPath("$.data.items", hasSize(2)));
	}

	@Test
	@DisplayName("貸出中の備品が1つもないとき：LENT は0件、IN_STOCK はすべて")
	void noLoans() throws Exception {
		equipment("A001", PC, "MANAGED", null, null, null, null);
		search(SOUMU, get("/api/equipment").param("loanStatus", "LENT"))
				.andExpect(jsonPath("$.data.items", hasSize(0)))
				.andExpect(jsonPath("$.data.totalCount").value(0))
				.andExpect(jsonPath("$.data.totalPages").value(0));
		search(SOUMU, get("/api/equipment").param("loanStatus", "IN_STOCK"))
				.andExpect(jsonPath("$.data.items[*].managementNo", contains("A001")));
	}

	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@Transactional
	@DisplayName("確認の条件は、管理中の備品にだけ効かせる")
	class Confirm {

		@BeforeEach
		void data() {
			equipment("A001", PC, "MANAGED", today, null, null, null); // 管理中・今月確認済み
			equipment("A002", PC, "MANAGED", lastMonth, null, null, null); // 管理中・先月
			equipment("A003", PC, "MANAGED", null, null, null, null); // 管理中・未記入
			equipment("A004", PC, "MISSING", today, null, null, null); // 不明・今月確認済み
			equipment("A005", PC, "MISSING", lastMonth, null, null, null); // 不明・先月
		}

		@Test
		@DisplayName("CONFIRMED：管理中で今月確認済みのもの＋不明はすべて")
		void confirmed() throws Exception {
			search(SOUMU, get("/api/equipment").param("confirm", "CONFIRMED"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A001", "A004", "A005")));
		}

		@Test
		@DisplayName("UNCONFIRMED：管理中で未確認（先月以前・未記入）のもの＋不明はすべて")
		void unconfirmed() throws Exception {
			search(SOUMU, get("/api/equipment").param("confirm", "UNCONFIRMED"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A002", "A003", "A004", "A005")));
		}

		@Test
		@DisplayName("棚卸対象（status=MANAGED・MISSING、confirm=UNCONFIRMED）：管理中で未確認＋不明のもの全部（画面設計 10章）")
		void inventory() throws Exception {
			jdbc.update("INSERT INTO equipment (management_no, category_id, status, disposed_on, created_at, updated_at)"
					+ " VALUES ('A006', 1, 'DISPOSED', '2026-01-01', NOW(), NOW())");
			search(SOUMU, get("/api/equipment").param("status", "MANAGED").param("status", "MISSING")
					.param("confirm", "UNCONFIRMED"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A002", "A003", "A004", "A005")));
		}
	}

	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@Transactional
	@DisplayName("キーワード")
	class Keyword {

		@Test
		@DisplayName("管理番号・メーカー・モデル・シリアルNoのどれかに含まれていれば出す。大文字・小文字を区別しない")
		void columns() throws Exception {
			equipment("PC-001", PC, "MANAGED", null, null, null, null);
			equipment("A002", PC, "MANAGED", null, "Lenovo", null, null);
			equipment("A003", PC, "MANAGED", null, null, "ThinkPad", null);
			equipment("A004", PC, "MANAGED", null, null, null, "SN-XYZ");
			equipment("A005", PC, "MANAGED", null, "Dell", "Latitude", "SN-1");
			search(SOUMU, get("/api/equipment").param("keyword", "pc-0"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("PC-001")));
			search(SOUMU, get("/api/equipment").param("keyword", "LENOVO"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A002")));
			search(SOUMU, get("/api/equipment").param("keyword", "thinkpad"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A003")));
			search(SOUMU, get("/api/equipment").param("keyword", " xyz "))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A004")));
			// 空白だけは条件にしない
			search(SOUMU, get("/api/equipment").param("keyword", "  "))
					.andExpect(jsonPath("$.data.items", hasSize(5)));
		}

		@Test
		@DisplayName("借用者の氏名でも探せる。今借りている人だけで、返した人の氏名では出ない")
		void borrower() throws Exception {
			long lent = equipment("A001", PC, "MANAGED", null, null, null, null);
			loan(lent, user("山田太郎"), null, null);
			long returned = equipment("A002", PC, "MANAGED", null, null, null, null);
			loan(returned, user("山田花子"), today, null);
			equipment("A003", PC, "MANAGED", null, "山田製作所", null, null);
			search(SOUMU, get("/api/equipment").param("keyword", "山田"))
					.andExpect(jsonPath("$.data.items[*].managementNo", containsInAnyOrder("A001", "A003")));
			search(SOUMU, get("/api/equipment").param("keyword", "太郎"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A001")))
					.andExpect(jsonPath("$.data.items[0].borrowerName").value("山田太郎"));
			search(SOUMU, get("/api/equipment").param("keyword", "花子"))
					.andExpect(jsonPath("$.data.items", hasSize(0)));
		}

		@Test
		@DisplayName("% や _ や ! も、ふつうの文字として探す")
		void specialCharacters() throws Exception {
			equipment("A001", PC, "MANAGED", null, "50%OFF", null, null);
			equipment("A002", PC, "MANAGED", null, "5000", null, null);
			equipment("A_3", PC, "MANAGED", null, null, null, null);
			equipment("AB3", PC, "MANAGED", null, "Wow!", null, null);
			search(SOUMU, get("/api/equipment").param("keyword", "50%"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A001")));
			search(SOUMU, get("/api/equipment").param("keyword", "a_3"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("A_3")));
			search(SOUMU, get("/api/equipment").param("keyword", "w!"))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("AB3")));
		}

		@Test
		@DisplayName("ほかの条件と組み合わせる（かつ）")
		void withOthers() throws Exception {
			equipment("A001", PC, "MANAGED", null, "Lenovo", null, null);
			equipment("C001", MOUSE, "MANAGED", null, "Lenovo", null, null);
			search(SOUMU, get("/api/equipment").param("keyword", "lenovo").param("categoryId", String.valueOf(MOUSE)))
					.andExpect(jsonPath("$.data.items[*].managementNo", contains("C001")));
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "status=FOO", "status=managed", "loanStatus=X", "confirm=X", "sort=maker",
			"sort=managementNo,asc", "page=0", "page=-1", "page=abc", "categoryId=abc" })
	@DisplayName("決まった値でない条件：400「入力内容に誤りがあります。」、errors は null")
	void invalid(String query) throws Exception {
		String[] pair = query.split("=", 2);
		search(SOUMU, get("/api/equipment").param(pair[0], pair[1]))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	// ここから下は、テストのデータを入れる道具

	ResultActions search(String email, MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request.with(login(email)));
	}

	RequestPostProcessor login(String email) {
		long userId = jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
		return authentication(new UsernamePasswordAuthenticationToken(new LoginUser(userId, LocalDateTime.now()),
				null, List.of()));
	}

	/** 備品を1つ入れ、ID を返す（廃棄の備品は、廃棄日が要るので、テストの中で直接入れる） */
	long equipment(String managementNo, long categoryId, String status, LocalDate lastConfirmedOn, String maker,
			String model, String serialNo) {
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, last_confirmed_on, maker, model,"
				+ " serial_no, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", managementNo,
				categoryId, status, lastConfirmedOn, maker, model, serialNo);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	/** ユーザを1人入れ、ID を返す（権限は一般社員） */
	long user(String name) {
		jdbc.update("INSERT INTO users (name, email, password_hash, role_id, created_at, updated_at)"
				+ " VALUES (?, ?, 'x', 2, NOW(), NOW())", name, name.hashCode() + "@example.com");
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	/** 貸出履歴を1つ入れる。返却日・取り消した日時が空なら貸出中 */
	void loan(long equipmentId, long userId, LocalDate returnedOn, LocalDateTime deletedAt) {
		LocalDate lentOn = lastMonth.minusDays(10);
		jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, returned_on, deleted_at, created_at, updated_at)"
				+ " VALUES (?, ?, ?, ?, ?, NOW(), NOW())", equipmentId, userId, lentOn, returnedOn, deletedAt);
	}
}
