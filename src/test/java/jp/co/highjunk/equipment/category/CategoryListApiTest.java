package jp.co.highjunk.equipment.category;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * K-01 種別の一覧（docs/API設計.md 3.6、詳細設計 category/K-01_種別一覧.md）のテスト。
 * 種別と備品は、テストごとに入れて、終わったら元に戻す（@Transactional。docs/テスト方針.md 4章）。
 * 最初のデータ（V2。データ設計 7章）に、種別が6つ（A：PC、B：ヘッドセット、C：マウス、D：カメラ、E：モニター、F：アダプタ）あり、備品はない。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("K-01 種別の一覧")
class CategoryListApiTest {

	static final String NOW = "'2026-11-10 10:00:00'";

	/** 最初のデータの種別の名前（頭文字の順） */
	static final String[] INITIAL_CATEGORIES = { "PC", "ヘッドセット", "マウス", "カメラ", "モニター", "アダプタ" };

	/** 最初のデータの PC（A）とマウス（C）の ID */
	static final long PC_ID = 1;

	static final long MOUSE_ID = 3;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	@WithMockUser
	@DisplayName("種別がないとき（全部削除済み）：200。data は空の配列")
	void empty() throws Exception {
		jdbc.update("UPDATE categories SET deleted_at = " + NOW);
		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data", hasSize(0)))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@WithMockUser
	@DisplayName("data に種別の配列をそのまま入れ、1件は id・name・initial・equipmentCount・version の5項目")
	void fields() throws Exception {
		jdbc.update("UPDATE categories SET version = 3 WHERE id = ?", PC_ID);
		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(6)))
				.andExpect(jsonPath("$.data[0].length()").value(5))
				.andExpect(jsonPath("$.data[0].id").value(PC_ID))
				.andExpect(jsonPath("$.data[0].name").value("PC"))
				.andExpect(jsonPath("$.data[0].initial").value("A"))
				.andExpect(jsonPath("$.data[0].equipmentCount").value(0))
				.andExpect(jsonPath("$.data[0].version").value(3));
	}

	@Test
	@WithMockUser
	@DisplayName("頭文字の順に並べ、頭文字がないものは後ろに名前の順（画面設計 16章）。頭文字がない種別の initial は null")
	void order() throws Exception {
		// ID の順（入れた順）と、並び順が違うように入れる
		insertCategory("ケーブル", null, null);
		insertCategory("スキャナ", "G", null);
		insertCategory("イス", null, null);
		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[*].name").value(contains("PC", "ヘッドセット", "マウス", "カメラ", "モニター",
						"アダプタ", "スキャナ", "イス", "ケーブル")))
				.andExpect(jsonPath("$.data[6].initial").value("G"))
				.andExpect(jsonPath("$.data[7].initial").value(nullValue()))
				.andExpect(jsonPath("$.data[8].initial").value(nullValue()));
	}

	@Test
	@WithMockUser
	@DisplayName("使っている備品の数：削除されていない備品を数える。管理中・廃棄・不明のどれも数え、削除済みの備品は数えない。備品がなければ 0")
	void equipmentCount() throws Exception {
		insertEquipment("A001", PC_ID, "MANAGED", null, null);
		insertEquipment("A002", PC_ID, "DISPOSED", "'2026-10-01'", null);
		insertEquipment("A003", PC_ID, "MISSING", null, null);
		insertEquipment("A004", PC_ID, "MANAGED", null, NOW);
		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].id").value(PC_ID))
				.andExpect(jsonPath("$.data[0].equipmentCount").value(3))
				.andExpect(jsonPath("$.data[2].id").value(MOUSE_ID))
				.andExpect(jsonPath("$.data[2].equipmentCount").value(0));
	}

	@Test
	@WithMockUser
	@DisplayName("削除済みの種別は出さない")
	void deletedCategory() throws Exception {
		insertCategory("古い種別", "Z", NOW);
		jdbc.update("UPDATE categories SET deleted_at = " + NOW + " WHERE id = ?", MOUSE_ID);
		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[*].name").value(contains("PC", "ヘッドセット", "カメラ", "モニター", "アダプタ")));
	}

	@Test
	@WithMockUser(authorities = { "LOAN_SELF", "CONFIRM_SELF" })
	@DisplayName("権限項目は要らない：一般社員の権限項目（LOAN_SELF・CONFIRM_SELF）だけでも 200")
	void anyLoggedInUser() throws Exception {
		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[*].name").value(contains(INITIAL_CATEGORIES)));
	}

	@Test
	@DisplayName("ログインしていなければ 401（ログインしてください。）。data・errors は null")
	void notLoggedIn() throws Exception {
		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("ログインしてください。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	/** 種別を1つ入れて、ID を返す。deletedAt は SQL の値（null なら削除されていない）。 */
	private long insertCategory(String name, String initial, String deletedAt) {
		jdbc.update("INSERT INTO categories (name, initial, deleted_at, created_at, updated_at) VALUES (?, ?, "
				+ (deletedAt == null ? "NULL" : deletedAt) + ", " + NOW + ", " + NOW + ")", name, initial);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	/** 備品を1つ入れる。disposedOn・deletedAt は SQL の値（null なら空）。 */
	private void insertEquipment(String managementNo, long categoryId, String status, String disposedOn,
			String deletedAt) {
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, disposed_on, deleted_at, created_at, updated_at)"
				+ " VALUES (?, ?, ?, " + (disposedOn == null ? "NULL" : disposedOn) + ", "
				+ (deletedAt == null ? "NULL" : deletedAt) + ", " + NOW + ", " + NOW + ")", managementNo, categoryId,
				status);
	}
}
