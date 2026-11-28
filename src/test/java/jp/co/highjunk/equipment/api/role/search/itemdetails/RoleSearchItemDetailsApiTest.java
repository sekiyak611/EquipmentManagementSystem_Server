package jp.co.highjunk.equipment.api.role.search.itemdetails;

import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.empty;
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
 * H-02 権限1つを返す（API設計 3.8、詳細設計 role/H-02_権限1つを返す.md）のテスト。
 * 権限・権限項目は、テストごとに入れて、終わったら元に戻す（@Transactional。テスト方針 4章）。
 * 最初のデータ（V2。データ設計 7章）に、権限が2つ（1：総務＝18項目すべて、2：一般社員＝LOAN_SELF・CONFIRM_SELF）ある。
 * 期待する値は、実装ではなく、設計書（H-02 の3・5・6・9・10章、データ設計 5.3・7章）から決めた。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("H-02 権限1つを返す")
class RoleSearchItemDetailsApiTest {

	static final String NOW = "'2026-11-10 10:00:00'";

	static final long SOUMU_ROLE_ID = 1;

	static final long IPPAN_ROLE_ID = 2;

	static final String NOT_FOUND = "対象が見つかりません。";

	/** 権限項目のコードを、並び順（データ設計 5.3 の表の上から。sort_order 1〜18）に並べたもの */
	static final String[] ALL_CODES = { "EQUIPMENT_VIEW", "EQUIPMENT_CREATE", "EQUIPMENT_UPDATE", "EQUIPMENT_DELETE",
			"CATEGORY_MANAGE", "LOAN_SELF", "LOAN_MANAGE", "LOAN_HISTORY_VIEW", "CONFIRM_SELF", "CONFIRM_ALL",
			"CONFIRM_CSV", "REMINDER_LOG_VIEW", "USER_VIEW", "USER_CREATE", "USER_UPDATE", "USER_DELETE",
			"PERMISSION_VIEW", "PERMISSION_MANAGE" };

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	@WithMockUser(authorities = "PERMISSION_VIEW")
	@DisplayName("総務の権限：200。data は id・name・permissionCodes・version の4項目。権限項目は18件すべて、並び順（sort_order）どおり")
	void soumu() throws Exception {
		mockMvc.perform(get("/api/roles/" + SOUMU_ROLE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data.length()").value(4))
				.andExpect(jsonPath("$.data.id").value(SOUMU_ROLE_ID))
				.andExpect(jsonPath("$.data.name").value("総務"))
				.andExpect(jsonPath("$.data.permissionCodes", hasSize(18)))
				.andExpect(jsonPath("$.data.permissionCodes").value(contains(ALL_CODES)))
				.andExpect(jsonPath("$.data.version").value(0))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@WithMockUser(authorities = "PERMISSION_VIEW")
	@DisplayName("一般社員の権限：LOAN_SELF と CONFIRM_SELF の2件だけ（ほかの権限の項目が混ざらない）")
	void ippan() throws Exception {
		mockMvc.perform(get("/api/roles/" + IPPAN_ROLE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(IPPAN_ROLE_ID))
				.andExpect(jsonPath("$.data.name").value("一般社員"))
				.andExpect(jsonPath("$.data.permissionCodes").value(contains("LOAN_SELF", "CONFIRM_SELF")))
				.andExpect(jsonPath("$.data.version").value(0));
	}

	@Test
	@WithMockUser(authorities = "PERMISSION_VIEW")
	@DisplayName("権限項目は、入れた順や名前の順ではなく、並び順（sort_order）で返す")
	void orderBySortOrder() throws Exception {
		// 名前の順（CONFIRM_SELF → EQUIPMENT_VIEW → LOAN_SELF → PERMISSION_MANAGE）とも、入れた順とも違う並び順で入れる
		long id = insertRole("並びの確認", null);
		for (String code : new String[] { "PERMISSION_MANAGE", "CONFIRM_SELF", "LOAN_SELF", "EQUIPMENT_VIEW" }) {
			insertRolePermission(id, code);
		}
		mockMvc.perform(get("/api/roles/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.permissionCodes")
						.value(contains("EQUIPMENT_VIEW", "LOAN_SELF", "CONFIRM_SELF", "PERMISSION_MANAGE")));
	}

	@Test
	@WithMockUser(authorities = "PERMISSION_VIEW")
	@DisplayName("権限項目が0件の権限：200。permissionCodes は null ではなく空の配列")
	void noPermissions() throws Exception {
		long id = insertRole("項目なし", null);
		mockMvc.perform(get("/api/roles/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(id))
				.andExpect(jsonPath("$.data.name").value("項目なし"))
				.andExpect(jsonPath("$.data.permissionCodes", empty()));
	}

	@Test
	@WithMockUser(authorities = "PERMISSION_VIEW")
	@DisplayName("version は、権限の今の version（H-04 で送り返す番号）")
	void version() throws Exception {
		jdbc.update("UPDATE roles SET version = 7 WHERE id = ?", IPPAN_ROLE_ID);
		mockMvc.perform(get("/api/roles/" + IPPAN_ROLE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.version").value(7));
	}

	@Test
	@WithMockUser(authorities = "PERMISSION_VIEW")
	@DisplayName("PERMISSION_VIEW だけを持つ人：200")
	void permissionViewOnly() throws Exception {
		mockMvc.perform(get("/api/roles/" + IPPAN_ROLE_ID)).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = "PERMISSION_MANAGE")
	@DisplayName("PERMISSION_MANAGE だけを持つ人：200（どちらか1つあれば呼べる）")
	void permissionManageOnly() throws Exception {
		mockMvc.perform(get("/api/roles/" + IPPAN_ROLE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("一般社員"));
	}

	@Test
	@WithMockUser(authorities = { "LOAN_SELF", "CONFIRM_SELF", "USER_VIEW" })
	@DisplayName("PERMISSION_VIEW・PERMISSION_MANAGE のどちらもない人：403（この操作を行う権限がありません。）。data・errors は null")
	void forbidden() throws Exception {
		mockMvc.perform(get("/api/roles/" + IPPAN_ROLE_ID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value("この操作を行う権限がありません。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@DisplayName("ログインしていなければ 401（ログインしてください。）。data・errors は null")
	void notLoggedIn() throws Exception {
		mockMvc.perform(get("/api/roles/" + IPPAN_ROLE_ID))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("ログインしてください。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@WithMockUser(authorities = "PERMISSION_VIEW")
	@DisplayName("ない権限：404（対象が見つかりません。）。data・errors は null")
	void notFound() throws Exception {
		mockMvc.perform(get("/api/roles/9999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value(NOT_FOUND))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@WithMockUser(authorities = "PERMISSION_VIEW")
	@DisplayName("削除済みの権限：404（権限項目が残っていても返さない）。同じ名前で作り直した権限は、別のものとして返せる")
	void deletedRole() throws Exception {
		long deletedId = insertRole("削除済み", NOW);
		insertRolePermission(deletedId, "LOAN_SELF");
		mockMvc.perform(get("/api/roles/" + deletedId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value(NOT_FOUND))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));

		long newId = insertRole("削除済み", null);
		insertRolePermission(newId, "CONFIRM_SELF");
		mockMvc.perform(get("/api/roles/" + newId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(newId))
				.andExpect(jsonPath("$.data.permissionCodes").value(contains("CONFIRM_SELF")));
	}

	@Test
	@WithMockUser(authorities = "PERMISSION_VIEW")
	@DisplayName("パスの id が数でない：400（入力内容に誤りがあります。）")
	void idNotNumber() throws Exception {
		mockMvc.perform(get("/api/roles/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@WithMockUser(authorities = "LOAN_SELF")
	@DisplayName("権限がない人が id に文字を入れた：403 ではなく 400（引数の変換が、権限の確認より先に起きる。H-02 の9章の順番）")
	void idNotNumberWithoutPermission() throws Exception {
		mockMvc.perform(get("/api/roles/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"));
	}

	@Test
	@DisplayName("ログインしていない人が id に文字を入れた：400 ではなく 401（ログインの確認が先。H-02 の9章の順番）")
	void idNotNumberNotLoggedIn() throws Exception {
		mockMvc.perform(get("/api/roles/abc"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("ログインしてください。"));
	}

	@Test
	@WithMockUser(authorities = "PERMISSION_VIEW")
	@DisplayName("読むだけ：呼んでも、権限・権限項目の対応の中身は変わらない")
	void readOnly() throws Exception {
		int roles = jdbc.queryForObject("SELECT COUNT(*) FROM roles", Integer.class);
		int rolePermissions = jdbc.queryForObject("SELECT COUNT(*) FROM role_permissions", Integer.class);
		String updatedAt = jdbc.queryForObject("SELECT CAST(updated_at AS CHAR) FROM roles WHERE id = ?", String.class,
				SOUMU_ROLE_ID);
		mockMvc.perform(get("/api/roles/" + SOUMU_ROLE_ID)).andExpect(status().isOk());
		assertEquals(roles, jdbc.queryForObject("SELECT COUNT(*) FROM roles", Integer.class));
		assertEquals(rolePermissions, jdbc.queryForObject("SELECT COUNT(*) FROM role_permissions", Integer.class));
		assertEquals(updatedAt,
				jdbc.queryForObject("SELECT CAST(updated_at AS CHAR) FROM roles WHERE id = ?", String.class,
						SOUMU_ROLE_ID));
	}

	/** 権限を1つ入れて、ID を返す。deletedAt は SQL の値（null なら削除されていない）。 */
	private long insertRole(String name, String deletedAt) {
		jdbc.update("INSERT INTO roles (name, is_default, deleted_at, created_at, updated_at) VALUES (?, FALSE, "
				+ (deletedAt == null ? "NULL" : deletedAt) + ", " + NOW + ", " + NOW + ")", name);
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	private void insertRolePermission(long roleId, String code) {
		jdbc.update("INSERT INTO role_permissions (role_id, permission_code) VALUES (?, ?)", roleId, code);
	}
}
