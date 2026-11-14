package jp.co.highjunk.equipment.api.equipment.category.update.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.validation.ServiceInDtoChecker;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;
import jp.co.highjunk.equipment.testsupport.EquipmentCategoryApiTestBase;

/**
 * K-03 種別を変える（docs/API設計.md 2.7・3.6、詳細設計 equipment/category/K-03_種別を変える.md）のテスト。
 * ログインのしかたと最初のデータは testsupport.EquipmentCategoryApiTestBase。最初のデータの種別の version は 0。
 */
@DisplayName("K-03 種別を変える")
class EquipmentCategoryUpdateItemApiTest extends EquipmentCategoryApiTestBase {

	static final String CONFLICT = "ほかの人が先にこの種別を更新しました。画面を開き直して、最新の内容を確かめてください。";

	@Test
	@DisplayName("総務：200。data に変えた種別（K-01 の1件と同じ形。version は1増え、使っている備品の数は今の数）。データベースの名前・頭文字・version・変えた人が変わり、作った人は変わらない")
	void update() throws Exception {
		insertEquipment("A001", PC_ID);
		insertEquipment("A002", PC_ID);
		Map<String, Object> before = row(PC_ID);
		update(SOUMU, PC_ID, "{\"name\":\"ノートPC\",\"initial\":\"N\",\"version\":0}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data.length()").value(5))
				.andExpect(jsonPath("$.data.id").value(PC_ID))
				.andExpect(jsonPath("$.data.name").value("ノートPC"))
				.andExpect(jsonPath("$.data.initial").value("N"))
				.andExpect(jsonPath("$.data.equipmentCount").value(2))
				.andExpect(jsonPath("$.data.version").value(1))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		Map<String, Object> after = row(PC_ID);
		assertThat(after.get("name")).isEqualTo("ノートPC");
		assertThat(after.get("initial")).isEqualTo("N");
		assertThat(after.get("version")).isEqualTo(1);
		assertThat(after.get("updated_by")).isEqualTo(userId(SOUMU));
		assertThat(after.get("created_by")).isEqualTo(before.get("created_by"));
		assertThat(after.get("created_at")).isEqualTo(before.get("created_at"));
	}

	@Test
	@DisplayName("名前・頭文字を変えずに保存しても 200。version は1増える。自分と同じ名前・頭文字は重なりにならない")
	void sameValues() throws Exception {
		update(SOUMU, PC_ID, "{\"name\":\"PC\",\"initial\":\"A\",\"version\":0}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.version").value(1));
		assertThat(row(PC_ID).get("version")).isEqualTo(1);
	}

	@Test
	@DisplayName("続けて変えるときは、返ってきた version を送る（古い version のままなら 409）")
	void updateTwice() throws Exception {
		update(SOUMU, PC_ID, "{\"name\":\"PC2\",\"initial\":\"A\",\"version\":0}").andExpect(status().isOk());
		update(SOUMU, PC_ID, "{\"name\":\"PC3\",\"initial\":\"A\",\"version\":1}").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.version").value(2));
		update(SOUMU, PC_ID, "{\"name\":\"PC4\",\"initial\":\"A\",\"version\":1}").andExpect(status().isConflict());
	}

	@Test
	@DisplayName("名前・頭文字の前後の空白を取り除き、頭文字の小文字は大文字にする。頭文字を空にすると、頭文字なし")
	void normalize() throws Exception {
		update(SOUMU, PC_ID, "{\"name\":\"　ノートPC \",\"initial\":\" n \",\"version\":0}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("ノートPC"))
				.andExpect(jsonPath("$.data.initial").value("N"));
		update(SOUMU, PC_ID, "{\"name\":\"ノートPC\",\"initial\":\"\",\"version\":1}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.initial").value(nullValue()));
		assertThat(row(PC_ID).get("initial")).isNull();
	}

	@Test
	@DisplayName("入力の誤り（名前が空・21文字、頭文字が英字1文字でない）：400。K-02 と同じ文。変えない")
	void invalidInput() throws Exception {
		update(SOUMU, PC_ID, "{\"name\":\" \",\"version\":0}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("name"))
				.andExpect(jsonPath("$.errors[0].message").value("名前を入力してください"));
		update(SOUMU, PC_ID, "{\"name\":\"" + "あ".repeat(21) + "\",\"version\":0}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].message").value("名前は20文字以内で入力してください"));
		update(SOUMU, PC_ID, "{\"name\":\"PC\",\"initial\":\"1\",\"version\":0}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("initial"))
				.andExpect(jsonPath("$.errors[0].message").value("頭文字は英字1文字で入力してください"));
		update(SOUMU, PC_ID, "{\"name\":\"" + "あ".repeat(20) + "\",\"version\":0}").andExpect(status().isOk());
		assertThat(row(PC_ID).get("version")).isEqualTo(1);
	}

	@Test
	@DisplayName("version がない：400（入力内容に誤りがあります。errors は null）。変えない")
	void noVersion() throws Exception {
		update(SOUMU, PC_ID, "{\"name\":\"ノートPC\",\"initial\":\"A\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(row(PC_ID).get("name")).isEqualTo("PC");
	}

	@Test
	@DisplayName("パスの ID が数でない：400")
	void idNotNumber() throws Exception {
		mockMvc.perform(put("/api/categories/abc").with(login(SOUMU)).with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"PC\",\"version\":0}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("名前・頭文字がほかの種別と重なる：400。errors に入力欄ごとの文（両方なら2つ）。変えない")
	void duplicated() throws Exception {
		update(SOUMU, PC_ID, "{\"name\":\"マウス\",\"initial\":\"A\",\"version\":0}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.length()").value(1))
				.andExpect(jsonPath("$.errors[0].field").value("name"))
				.andExpect(jsonPath("$.errors[0].message").value("この名前はすでに使われています"));
		update(SOUMU, PC_ID, "{\"name\":\"PC\",\"initial\":\"c\",\"version\":0}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.length()").value(1))
				.andExpect(jsonPath("$.errors[0].field").value("initial"))
				.andExpect(jsonPath("$.errors[0].message").value("この頭文字はすでに使われています"));
		update(SOUMU, PC_ID, "{\"name\":\"マウス\",\"initial\":\"C\",\"version\":0}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.length()").value(2));
		assertThat(row(PC_ID).get("version")).isEqualTo(0);
	}

	@Test
	@DisplayName("削除済みの種別と同じ名前・頭文字には変えられる")
	void reuseDeleted() throws Exception {
		jdbc.update("UPDATE categories SET deleted_at = NOW() WHERE id = ?", MOUSE_ID);
		update(SOUMU, PC_ID, "{\"name\":\"マウス\",\"initial\":\"C\",\"version\":0}").andExpect(status().isOk());
	}

	@Test
	@DisplayName("ほかの人が先に保存していた（version が違う）：409。画面設計 16章の文。変えない")
	void conflict() throws Exception {
		jdbc.update("UPDATE categories SET version = 5 WHERE id = ?", PC_ID);
		update(SOUMU, PC_ID, "{\"name\":\"ノートPC\",\"initial\":\"A\",\"version\":4}")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").value(CONFLICT))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(row(PC_ID).get("name")).isEqualTo("PC");
		assertThat(row(PC_ID).get("version")).isEqualTo(5);
	}

	@Test
	@DisplayName("ない種別・削除済みの種別：404「対象が見つかりません。」")
	void notFound() throws Exception {
		update(SOUMU, 9999, "{\"name\":\"ノートPC\",\"version\":0}")
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"));
		jdbc.update("UPDATE categories SET deleted_at = NOW() WHERE id = ?", PC_ID);
		update(SOUMU, PC_ID, "{\"name\":\"ノートPC\",\"version\":0}")
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("一般社員（CATEGORY_MANAGE がない）：403。変えない")
	void forbidden() throws Exception {
		update(IPPAN, PC_ID, "{\"name\":\"ノートPC\",\"initial\":\"A\",\"version\":0}")
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("この操作を行う権限がありません。"));
		assertThat(row(PC_ID).get("name")).isEqualTo("PC");
	}

	@Test
	@DisplayName("ログインしていない：401。CSRF の合言葉がない：403。どちらも変えない")
	void notLoggedInOrNoCsrf() throws Exception {
		mockMvc.perform(put("/api/categories/" + PC_ID).with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"ノートPC\",\"version\":0}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("ログインしてください。"));
		mockMvc.perform(put("/api/categories/" + PC_ID).with(login(SOUMU))
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"ノートPC\",\"version\":0}"))
				.andExpect(status().isForbidden());
		assertThat(row(PC_ID).get("name")).isEqualTo("PC");
	}

	/**
	 * 確かめ（ServiceInDto のチェック。ServiceInDtoChecker）のあとで、ほかの人が先に変えた・同じ名前にしていた場合（K-03 の4章の8）。
	 * 確かめを偽物にして素通りさせ、UPDATE の条件（version）とデータベースの重複禁止に当てる。
	 */
	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@Transactional
	@DisplayName("確かめのあとに、ほかの人が先に変えていたとき")
	class Race {

		@MockitoBean
		ServiceInDtoChecker serviceInDtoChecker;

		@Test
		@DisplayName("version が変わっていて UPDATE が0件：409。画面設計 16章の文")
		void versionChanged() throws Exception {
			update(SOUMU, PC_ID, "{\"name\":\"ノートPC\",\"initial\":\"A\",\"version\":7}")
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.message").value(CONFLICT));
			assertThat(row(PC_ID).get("name")).isEqualTo("PC");
		}

		@Test
		@DisplayName("名前が重複禁止に当たる：400。errors の name に「この名前はすでに使われています」")
		void duplicatedName() throws Exception {
			update(SOUMU, PC_ID, "{\"name\":\"マウス\",\"initial\":\"A\",\"version\":0}")
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors.length()").value(1))
					.andExpect(jsonPath("$.errors[0].field").value("name"))
					.andExpect(jsonPath("$.errors[0].message").value("この名前はすでに使われています"));
		}
	}

	ResultActions update(String email, long id, String json) throws Exception {
		return mockMvc.perform(put("/api/categories/" + id)
				.with(login(email))
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	Map<String, Object> row(long id) {
		return jdbc.queryForMap(
				"SELECT name, initial, version, created_by, created_at, updated_by FROM categories WHERE id = ?", id);
	}

	void insertEquipment(String managementNo, long categoryId) {
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, created_at, updated_at)"
				+ " VALUES (?, ?, 'MANAGED', NOW(), NOW())", managementNo, categoryId);
	}
}
