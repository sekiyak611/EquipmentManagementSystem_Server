package jp.co.highjunk.equipment.api.equipment.category.entry.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
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
 * K-02 種別を追加する（docs/API設計.md 3.6、詳細設計 equipment/category/K-02_種別を追加する.md）のテスト。
 * ログインのしかたと最初のデータは testsupport.EquipmentCategoryApiTestBase。
 */
@DisplayName("K-02 種別を追加する")
class EquipmentCategoryEntryItemApiTest extends EquipmentCategoryApiTestBase {

	@Test
	@DisplayName("総務：201。data に作った種別（K-01 の1件と同じ形。使っている備品の数は 0、version は 0）。データベースに、作った人・日時が入る")
	void create() throws Exception {
		long userId = userId(SOUMU);
		String body = create(SOUMU, "{\"name\":\"ケーブル\",\"initial\":\"G\"}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value(201))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data.length()").value(5))
				.andExpect(jsonPath("$.data.name").value("ケーブル"))
				.andExpect(jsonPath("$.data.initial").value("G"))
				.andExpect(jsonPath("$.data.equipmentCount").value(0))
				.andExpect(jsonPath("$.data.version").value(0))
				.andExpect(jsonPath("$.errors").value(nullValue()))
				.andReturn().getResponse().getContentAsString();

		Map<String, Object> row = jdbc.queryForMap(
				"SELECT id, name, initial, version, deleted_at, created_by, updated_by, created_at, updated_at"
						+ " FROM categories WHERE name = 'ケーブル'");
		assertThat(body).contains("\"id\":" + row.get("id"));
		assertThat(row.get("initial")).isEqualTo("G");
		assertThat(row.get("version")).isEqualTo(0);
		assertThat(row.get("deleted_at")).isNull();
		assertThat(row.get("created_by")).isEqualTo(userId);
		assertThat(row.get("updated_by")).isEqualTo(userId);
		assertThat((LocalDateTime) row.get("created_at")).isNotNull().isEqualTo(row.get("updated_at"));
	}

	@Test
	@DisplayName("頭文字の小文字は大文字に直す。名前・頭文字の前後の空白（全角も）は取り除く")
	void normalize() throws Exception {
		create(SOUMU, "{\"name\":\"　 ケーブル \",\"initial\":\" g \"}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.name").value("ケーブル"))
				.andExpect(jsonPath("$.data.initial").value("G"));
		assertThat(jdbc.queryForObject("SELECT initial FROM categories WHERE name = 'ケーブル'", String.class))
				.isEqualTo("G");
	}

	@Test
	@DisplayName("頭文字は任意：送らない・null・空・空白だけなら、頭文字なし（null）")
	void noInitial() throws Exception {
		create(SOUMU, "{\"name\":\"ケーブル1\"}").andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.initial").value(nullValue()));
		create(SOUMU, "{\"name\":\"ケーブル2\",\"initial\":null}").andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.initial").value(nullValue()));
		create(SOUMU, "{\"name\":\"ケーブル3\",\"initial\":\"\"}").andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.initial").value(nullValue()));
		create(SOUMU, "{\"name\":\"ケーブル4\",\"initial\":\"  \"}").andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.initial").value(nullValue()));
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM categories WHERE name LIKE 'ケーブル_' AND initial IS NULL",
				Integer.class)).isEqualTo(4);
	}

	@Test
	@DisplayName("名前が空・空白だけ・送らない：400「名前を入力してください」。足さない")
	void nameRequired() throws Exception {
		for (String json : List.of("{\"name\":\"\"}", "{\"name\":\" 　 \"}", "{}")) {
			create(SOUMU, json)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
					.andExpect(jsonPath("$.errors.length()").value(1))
					.andExpect(jsonPath("$.errors[0].field").value("name"))
					.andExpect(jsonPath("$.errors[0].message").value("名前を入力してください"));
		}
		assertThat(countCategories()).isEqualTo(6);
	}

	@Test
	@DisplayName("名前は20文字まで：20文字は 201、21文字は 400「名前は20文字以内で入力してください」。前後の空白は数えない")
	void nameLength() throws Exception {
		create(SOUMU, "{\"name\":\"" + "あ".repeat(20) + "\"}").andExpect(status().isCreated());
		create(SOUMU, "{\"name\":\"  " + "い".repeat(20) + "  \"}").andExpect(status().isCreated());
		create(SOUMU, "{\"name\":\"" + "う".repeat(21) + "\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("name"))
				.andExpect(jsonPath("$.errors[0].message").value("名前は20文字以内で入力してください"));
		assertThat(countCategories()).isEqualTo(8);
	}

	@Test
	@DisplayName("頭文字が英字1文字でない（2文字、数字、記号、全角の英字、ひらがな）：400「頭文字は英字1文字で入力してください」")
	void initialFormat() throws Exception {
		for (String initial : List.of("GH", "1", "-", "Ｇ", "ｇ", "あ")) {
			create(SOUMU, "{\"name\":\"ケーブル\",\"initial\":\"" + initial + "\"}")
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors.length()").value(1))
					.andExpect(jsonPath("$.errors[0].field").value("initial"))
					.andExpect(jsonPath("$.errors[0].message").value("頭文字は英字1文字で入力してください"));
		}
		assertThat(countCategories()).isEqualTo(6);
	}

	@Test
	@DisplayName("名前がほかの種別と重なる：400。errors の name に「この名前はすでに使われています」。足さない")
	void duplicatedName() throws Exception {
		create(SOUMU, "{\"name\":\"PC\",\"initial\":\"G\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors.length()").value(1))
				.andExpect(jsonPath("$.errors[0].field").value("name"))
				.andExpect(jsonPath("$.errors[0].message").value("この名前はすでに使われています"));
		assertThat(countCategories()).isEqualTo(6);
	}

	@Test
	@DisplayName("頭文字がほかの種別と重なる（小文字で送っても）：400。errors の initial に「この頭文字はすでに使われています」")
	void duplicatedInitial() throws Exception {
		create(SOUMU, "{\"name\":\"ケーブル\",\"initial\":\"a\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.length()").value(1))
				.andExpect(jsonPath("$.errors[0].field").value("initial"))
				.andExpect(jsonPath("$.errors[0].message").value("この頭文字はすでに使われています"));
		assertThat(countCategories()).isEqualTo(6);
	}

	@Test
	@DisplayName("名前と頭文字の両方が重なる：400。errors に name・initial の2つ")
	void duplicatedBoth() throws Exception {
		create(SOUMU, "{\"name\":\"PC\",\"initial\":\"A\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.length()").value(2))
				.andExpect(jsonPath("$.errors[0].field").value("name"))
				.andExpect(jsonPath("$.errors[1].field").value("initial"));
	}

	@Test
	@DisplayName("大文字・小文字が違う名前は別の名前（「pc」は「PC」と重ならない）")
	void caseSensitiveName() throws Exception {
		create(SOUMU, "{\"name\":\"pc\"}").andExpect(status().isCreated());
	}

	@Test
	@DisplayName("削除済みの種別と同じ名前・頭文字は使える")
	void reuseDeleted() throws Exception {
		jdbc.update("UPDATE categories SET deleted_at = NOW() WHERE name = 'PC'");
		create(SOUMU, "{\"name\":\"PC\",\"initial\":\"A\"}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.name").value("PC"))
				.andExpect(jsonPath("$.data.initial").value("A"));
	}

	@Test
	@DisplayName("一般社員（CATEGORY_MANAGE がない）：403「この操作を行う権限がありません。」。足さない")
	void forbidden() throws Exception {
		create(IPPAN, "{\"name\":\"ケーブル\",\"initial\":\"G\"}")
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value("この操作を行う権限がありません。"));
		assertThat(countCategories()).isEqualTo(6);
	}

	@Test
	@DisplayName("ログインしていない：401「ログインしてください。」")
	void notLoggedIn() throws Exception {
		mockMvc.perform(post("/api/categories").with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"ケーブル\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("ログインしてください。"));
		assertThat(countCategories()).isEqualTo(6);
	}

	@Test
	@DisplayName("CSRF の合言葉がない：403。足さない")
	void noCsrfToken() throws Exception {
		mockMvc.perform(post("/api/categories").with(login(SOUMU))
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"ケーブル\"}"))
				.andExpect(status().isForbidden());
		assertThat(countCategories()).isEqualTo(6);
	}

	/**
	 * 確かめ（ServiceInDto のチェック。ServiceInDtoChecker）のあとで、ほかの人が同じ名前・頭文字を足していた場合（K-02 の8章）。
	 * 確かめを偽物にして素通りさせ、データベースの重複禁止に当てる。
	 */
	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@Transactional
	@DisplayName("確かめのあとに、ほかの人が同じ名前・頭文字を足していたとき")
	class Race {

		@MockitoBean
		ServiceInDtoChecker serviceInDtoChecker;

		@Test
		@DisplayName("名前が重複禁止に当たる：400。errors の name に「この名前はすでに使われています」")
		void name() throws Exception {
			create(SOUMU, "{\"name\":\"PC\",\"initial\":\"G\"}")
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors.length()").value(1))
					.andExpect(jsonPath("$.errors[0].field").value("name"))
					.andExpect(jsonPath("$.errors[0].message").value("この名前はすでに使われています"));
		}

		@Test
		@DisplayName("頭文字が重複禁止に当たる：400。errors の initial に「この頭文字はすでに使われています」")
		void initial() throws Exception {
			create(SOUMU, "{\"name\":\"ケーブル\",\"initial\":\"A\"}")
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors.length()").value(1))
					.andExpect(jsonPath("$.errors[0].field").value("initial"))
					.andExpect(jsonPath("$.errors[0].message").value("この頭文字はすでに使われています"));
		}
	}

	ResultActions create(String email, String json) throws Exception {
		return mockMvc.perform(post("/api/categories")
				.with(login(email))
				.with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}
}
