package jp.co.highjunk.equipment.api.equipment.category.delete.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;
import jp.co.highjunk.equipment.testsupport.EquipmentCategoryApiTestBase;

/**
 * K-04 種別を消す（docs/API設計.md 3.6、詳細設計 equipment/category/K-04_種別を消す.md）のテスト。
 * ログインのしかたと最初のデータは testsupport.EquipmentCategoryApiTestBase。
 */
@DisplayName("K-04 種別を消す")
class EquipmentCategoryDeleteItemApiTest extends EquipmentCategoryApiTestBase {

	static final String IN_USE = "この種別の備品があるため削除できません";

	@Test
	@DisplayName("総務・使っている備品がない：200（data は null）。論理削除され（deleted_at・変えた人が入り、version が1増える）、一覧（K-01）に出なくなる")
	void deleted() throws Exception {
		deleteAs(SOUMU, MOUSE_ID)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		Map<String, Object> row = row(MOUSE_ID);
		assertThat(row.get("deleted_at")).isNotNull();
		assertThat(row.get("updated_at")).isEqualTo(row.get("deleted_at"));
		assertThat(row.get("updated_by")).isEqualTo(userId(SOUMU));
		assertThat(row.get("version")).isEqualTo(1);
		assertThat(countCategories()).isEqualTo(6);
		mockMvc.perform(get("/api/categories").with(login(SOUMU)))
				.andExpect(jsonPath("$.data.length()").value(5))
				.andExpect(jsonPath("$.data[?(@.id == " + MOUSE_ID + ")]").isEmpty());
	}

	@Test
	@DisplayName("使っている備品がある（管理中・廃棄・不明のどれでも）：400「この種別の備品があるため削除できません」。消さない")
	void inUse() throws Exception {
		for (String status : new String[] { "MANAGED", "DISPOSED", "MISSING" }) {
			long id = jdbc.queryForObject("SELECT id FROM categories WHERE name = 'マウス'", Long.class);
			jdbc.update("DELETE FROM equipment");
			insertEquipment("C001", id, status, null);
			deleteAs(SOUMU, id)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.message").value(IN_USE))
					.andExpect(jsonPath("$.data").value(nullValue()))
					.andExpect(jsonPath("$.errors").value(nullValue()));
			assertThat(row(id).get("deleted_at")).isNull();
		}
	}

	@Test
	@DisplayName("使っていた備品が全部削除済みなら、消せる")
	void onlyDeletedEquipment() throws Exception {
		insertEquipment("C001", MOUSE_ID, "MANAGED", "NOW()");
		deleteAs(SOUMU, MOUSE_ID).andExpect(status().isOk());
		assertThat(row(MOUSE_ID).get("deleted_at")).isNotNull();
	}

	@Test
	@DisplayName("消した種別の名前・頭文字は、新しい種別で使える（K-02）")
	void reuseNameAfterDelete() throws Exception {
		deleteAs(SOUMU, MOUSE_ID).andExpect(status().isOk());
		mockMvc.perform(post("/api/categories")
				.with(login(SOUMU)).with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"マウス\",\"initial\":\"C\"}"))
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("ない種別・削除済みの種別（2回消す）：404「対象が見つかりません。」")
	void notFound() throws Exception {
		deleteAs(SOUMU, 9999)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"));
		deleteAs(SOUMU, MOUSE_ID).andExpect(status().isOk());
		deleteAs(SOUMU, MOUSE_ID).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("消したあとで変えよう（K-03）としても 404")
	void updateAfterDelete() throws Exception {
		deleteAs(SOUMU, MOUSE_ID).andExpect(status().isOk());
		mockMvc.perform(put("/api/categories/" + MOUSE_ID)
				.with(login(SOUMU)).with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"マウス2\",\"version\":1}"))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("パスの ID が数でない：400")
	void idNotNumber() throws Exception {
		mockMvc.perform(delete("/api/categories/abc").with(login(SOUMU)).with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("一般社員（CATEGORY_MANAGE がない）：403。消さない")
	void forbidden() throws Exception {
		deleteAs(IPPAN, MOUSE_ID)
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("この操作を行う権限がありません。"));
		assertThat(row(MOUSE_ID).get("deleted_at")).isNull();
	}

	@Test
	@DisplayName("ログインしていない：401。CSRF の合言葉がない：403。どちらも消さない")
	void notLoggedInOrNoCsrf() throws Exception {
		mockMvc.perform(delete("/api/categories/" + MOUSE_ID).with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("ログインしてください。"));
		mockMvc.perform(delete("/api/categories/" + MOUSE_ID).with(login(SOUMU)))
				.andExpect(status().isForbidden());
		assertThat(row(MOUSE_ID).get("deleted_at")).isNull();
	}

	ResultActions deleteAs(String email, long id) throws Exception {
		return mockMvc.perform(delete("/api/categories/" + id)
				.with(login(email))
				.with(CsrfTestHelper.csrfToken(mockMvc)));
	}

	Map<String, Object> row(long id) {
		return jdbc.queryForMap("SELECT deleted_at, updated_at, updated_by, version FROM categories WHERE id = ?", id);
	}

	/** 備品を1つ入れる。deletedAt は SQL の値（null なら削除されていない）。廃棄なら廃棄日も入れる。 */
	void insertEquipment(String managementNo, long categoryId, String status, String deletedAt) {
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, disposed_on, deleted_at, created_at, updated_at)"
				+ " VALUES (?, ?, ?, " + ("DISPOSED".equals(status) ? "'2026-10-01'" : "NULL") + ", "
				+ (deletedAt == null ? "NULL" : deletedAt) + ", NOW(), NOW())", managementNo, categoryId, status);
	}
}
