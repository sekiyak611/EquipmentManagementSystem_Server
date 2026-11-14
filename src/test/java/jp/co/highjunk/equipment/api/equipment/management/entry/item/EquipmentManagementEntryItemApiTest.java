package jp.co.highjunk.equipment.api.equipment.management.entry.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.security.LoginUser;
import jp.co.highjunk.equipment.common.validation.ServiceInDtoChecker;
import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;

/**
 * C-03 備品を登録する（docs/API設計.md 3.3、詳細設計 equipment/management/C-03_備品を登録する.md）のテスト。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional）。今日は、アプリの時計（Clock）の今日。
 * 総務 soumu@example.com は EQUIPMENT_CREATE を持ち、一般社員 ippan@example.com は持たない。最初のデータの種別：1 PC、3 マウス。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("C-03 備品を登録する")
class EquipmentManagementEntryItemApiTest {

	static final String SOUMU = "soumu@example.com";

	static final String IPPAN = "ippan@example.com";

	static final long PC = 1;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	Clock clock;

	LocalDate today;

	@BeforeEach
	void setUp() {
		today = LocalDate.now(clock);
	}

	@Test
	@DisplayName("ログインしていない：401")
	void notLoggedIn() throws Exception {
		mockMvc.perform(post("/api/equipment").with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("EQUIPMENT_CREATE がない（一般社員）：403。登録しない")
	void forbidden() throws Exception {
		create(IPPAN, "{\"managementNo\":\"A001\",\"categoryId\":1}")
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("この操作を行う権限がありません。"));
		assertThat(count()).isZero();
	}

	@Test
	@DisplayName("すべての項目：201。data は C-01 の1件と同じ形。管理状態は管理中、最終確認日は今日、version は 0")
	void allFields() throws Exception {
		create(SOUMU, """
				{"managementNo":"E015","categoryId":1,"maker":"EIZO","model":"EV2485","serialNo":"SN-1",
				 "size":"24インチ","resolution":"1920×1200","heightAdjustable":true,"pivot":false,
				 "purchasedOn":"2024-04-01","warrantyUntil":"2027-03-31","note":"会議室用",
				 "status":"DISPOSED","lastConfirmedOn":"2020-01-01"}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value(201))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data.length()").value(10))
				.andExpect(jsonPath("$.data.id").isNumber())
				.andExpect(jsonPath("$.data.managementNo").value("E015"))
				.andExpect(jsonPath("$.data.categoryName").value("PC"))
				.andExpect(jsonPath("$.data.maker").value("EIZO"))
				.andExpect(jsonPath("$.data.model").value("EV2485"))
				.andExpect(jsonPath("$.data.status").value("MANAGED"))
				.andExpect(jsonPath("$.data.loanStatus").value("IN_STOCK"))
				.andExpect(jsonPath("$.data.borrowerName").value(nullValue()))
				.andExpect(jsonPath("$.data.lastConfirmedOn").value(today.toString()))
				.andExpect(jsonPath("$.data.confirmedThisMonth").value(true))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		Map<String, Object> row = jdbc.queryForMap("SELECT * FROM equipment WHERE management_no = 'E015'");
		assertThat(row.get("category_id")).isEqualTo(1L);
		assertThat(row.get("serial_no")).isEqualTo("SN-1");
		assertThat(row.get("size")).isEqualTo("24インチ");
		assertThat(row.get("resolution")).isEqualTo("1920×1200");
		assertThat(row.get("height_adjustable")).isEqualTo(true);
		assertThat(row.get("pivot")).isEqualTo(false);
		assertThat(row.get("purchased_on").toString()).isEqualTo("2024-04-01");
		assertThat(row.get("warranty_until").toString()).isEqualTo("2027-03-31");
		assertThat(row.get("note")).isEqualTo("会議室用");
		// 送られた管理状態・最終確認日は使わない
		assertThat(row.get("status")).isEqualTo("MANAGED");
		assertThat(row.get("last_confirmed_on").toString()).isEqualTo(today.toString());
		assertThat(row.get("disposed_on")).isNull();
		assertThat(row.get("version")).isEqualTo(0);
		assertThat(row.get("created_by")).isEqualTo(userId(SOUMU));
		assertThat(row.get("updated_by")).isEqualTo(userId(SOUMU));
		assertThat(row.get("deleted_at")).isNull();
	}

	@Test
	@DisplayName("必須だけ：ほかの項目は null。昇降・ピボットは未設定（null）")
	void requiredOnly() throws Exception {
		create(SOUMU, "{\"managementNo\":\"C001\",\"categoryId\":3}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.categoryName").value("マウス"))
				.andExpect(jsonPath("$.data.maker").value(nullValue()));
		Map<String, Object> row = jdbc.queryForMap("SELECT * FROM equipment WHERE management_no = 'C001'");
		for (String column : List.of("maker", "model", "serial_no", "size", "resolution", "height_adjustable", "pivot",
				"purchased_on", "warranty_until", "note")) {
			assertThat(row.get(column)).as(column).isNull();
		}
	}

	@Test
	@DisplayName("文字の項目は前後の空白（全角も）を取り除き、空・空白だけなら null にする")
	void strip() throws Exception {
		create(SOUMU, "{\"managementNo\":\"　A001 \",\"categoryId\":1,\"maker\":\" Lenovo \",\"model\":\"  \","
				+ "\"serialNo\":\"\",\"note\":\"　\"}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.managementNo").value("A001"))
				.andExpect(jsonPath("$.data.maker").value("Lenovo"))
				.andExpect(jsonPath("$.data.model").value(nullValue()));
		Map<String, Object> row = jdbc.queryForMap("SELECT * FROM equipment WHERE management_no = 'A001'");
		assertThat(row.get("serial_no")).isNull();
		assertThat(row.get("note")).isNull();
	}

	@Test
	@DisplayName("文字数の上限ちょうどは登録できる")
	void maxLengths() throws Exception {
		create(SOUMU, "{\"managementNo\":\"" + "A".repeat(30) + "\",\"categoryId\":1,\"maker\":\"" + "あ".repeat(50)
				+ "\",\"model\":\"" + "m".repeat(100) + "\",\"serialNo\":\"" + "s".repeat(100) + "\",\"size\":\""
				+ "z".repeat(20) + "\",\"resolution\":\"" + "r".repeat(20) + "\",\"note\":\"" + "備".repeat(500) + "\"}")
				.andExpect(status().isCreated());
	}

	@ParameterizedTest(name = "{0}")
	@CsvSource(delimiter = '|', value = {
			"管理番号が空|managementNo|\"managementNo\":\" \",\"categoryId\":1|管理番号を入力してください",
			"管理番号がない|managementNo|\"categoryId\":1|管理番号を入力してください",
			"管理番号が31文字|managementNo|\"managementNo\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"categoryId\":1|管理番号は30文字以内で入力してください",
			"種別がない|categoryId|\"managementNo\":\"A001\"|種別を選んでください",
			"メーカーが51文字|maker|\"managementNo\":\"A001\",\"categoryId\":1,\"maker\":\"MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM\"|メーカーは50文字以内で入力してください",
			"サイズが21文字|size|\"managementNo\":\"A001\",\"categoryId\":1,\"size\":\"SSSSSSSSSSSSSSSSSSSSS\"|サイズは20文字以内で入力してください",
			"解像度が21文字|resolution|\"managementNo\":\"A001\",\"categoryId\":1,\"resolution\":\"RRRRRRRRRRRRRRRRRRRRR\"|解像度は20文字以内で入力してください" })
	@DisplayName("入力の誤り：400。errors に入力欄ごとの文（画面設計 12章）")
	void invalid(String name, String field, String json, String message) throws Exception {
		create(SOUMU, "{" + json + "}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.errors.length()").value(1))
				.andExpect(jsonPath("$.errors[0].field").value(field))
				.andExpect(jsonPath("$.errors[0].message").value(message));
		assertThat(count()).isZero();
	}

	@Test
	@DisplayName("モデル・シリアルNo が101文字、備考が501文字：それぞれの文")
	void tooLong() throws Exception {
		create(SOUMU, "{\"managementNo\":\"A001\",\"categoryId\":1,\"model\":\"" + "m".repeat(101) + "\",\"serialNo\":\""
				+ "s".repeat(101) + "\",\"note\":\"" + "n".repeat(501) + "\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.length()").value(3))
				.andExpect(jsonPath("$.errors[?(@.field=='model')].message").value("モデルは100文字以内で入力してください"))
				.andExpect(jsonPath("$.errors[?(@.field=='serialNo')].message").value("シリアルNoは100文字以内で入力してください"))
				.andExpect(jsonPath("$.errors[?(@.field=='note')].message").value("備考は500文字以内で入力してください"));
	}

	@Test
	@DisplayName("日付の形でない：400、errors は null")
	void badDate() throws Exception {
		create(SOUMU, "{\"managementNo\":\"A001\",\"categoryId\":1,\"purchasedOn\":\"2026/11/01\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@DisplayName("管理番号がほかの備品と重なる：400「この管理番号はすでに使われています」。削除済みの備品の番号は使い直せる")
	void duplicated() throws Exception {
		insert("A001", null);
		insert("A002", "NOW()");
		create(SOUMU, "{\"managementNo\":\"A001\",\"categoryId\":1}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("managementNo"))
				.andExpect(jsonPath("$.errors[0].message").value("この管理番号はすでに使われています"));
		create(SOUMU, "{\"managementNo\":\"A002\",\"categoryId\":1}").andExpect(status().isCreated());
		// 大文字・小文字は別の番号（データ設計 2章の照合順序）
		create(SOUMU, "{\"managementNo\":\"a001\",\"categoryId\":1}").andExpect(status().isCreated());
	}

	@Test
	@DisplayName("ない種別・削除された種別：400、種別の欄に「種別を選んでください」")
	void category() throws Exception {
		create(SOUMU, "{\"managementNo\":\"A001\",\"categoryId\":999}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("categoryId"))
				.andExpect(jsonPath("$.errors[0].message").value("種別を選んでください"));
		jdbc.update("UPDATE categories SET deleted_at = NOW() WHERE id = 3");
		create(SOUMU, "{\"managementNo\":\"A001\",\"categoryId\":3}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("categoryId"));
	}

	@Test
	@DisplayName("購入日：今日は登録でき、明日は「購入日は今日以前の日付を入力してください」")
	void purchasedOn() throws Exception {
		create(SOUMU, "{\"managementNo\":\"A001\",\"categoryId\":1,\"purchasedOn\":\"" + today + "\"}")
				.andExpect(status().isCreated());
		create(SOUMU, "{\"managementNo\":\"A002\",\"categoryId\":1,\"purchasedOn\":\"" + today.plusDays(1) + "\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("purchasedOn"))
				.andExpect(jsonPath("$.errors[0].message").value("購入日は今日以前の日付を入力してください"));
	}

	@Test
	@DisplayName("保証期日：購入日と同じ日は登録でき、前の日は「保証期日は購入日以降の日付を入力してください」。購入日がなければ確かめない")
	void warrantyUntil() throws Exception {
		create(SOUMU, "{\"managementNo\":\"A001\",\"categoryId\":1,\"purchasedOn\":\"2026-04-01\",\"warrantyUntil\":\"2026-04-01\"}")
				.andExpect(status().isCreated());
		create(SOUMU, "{\"managementNo\":\"A002\",\"categoryId\":1,\"purchasedOn\":\"2026-04-01\",\"warrantyUntil\":\"2026-03-31\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("warrantyUntil"))
				.andExpect(jsonPath("$.errors[0].message").value("保証期日は購入日以降の日付を入力してください"));
		create(SOUMU, "{\"managementNo\":\"A003\",\"categoryId\":1,\"warrantyUntil\":\"2000-01-01\"}")
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("誤りが重なると、まず管理番号・種別（ServiceInDto）を返し、それが直ってから購入日・保証期日（Validator）を返す")
	void manyErrors() throws Exception {
		// 2026-11-15 に、確かめを ServiceInDto（DB との整合性）と Validator（今日の日付が要るもの）に分けたので、2回に分かれて返る
		insert("A001", null);
		create(SOUMU, "{\"managementNo\":\"A001\",\"categoryId\":999,\"purchasedOn\":\"" + today.plusDays(1)
				+ "\",\"warrantyUntil\":\"" + today + "\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[*].field").value(org.hamcrest.Matchers.contains("managementNo",
						"categoryId")));
		create(SOUMU, "{\"managementNo\":\"A009\",\"categoryId\":1,\"purchasedOn\":\"" + today.plusDays(1)
				+ "\",\"warrantyUntil\":\"" + today + "\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[*].field").value(org.hamcrest.Matchers.contains("purchasedOn",
						"warrantyUntil")));
	}

	/**
	 * 確かめ（ServiceInDto のチェック。ServiceInDtoChecker）のあとで、ほかの人が同じ管理番号の備品を足していた場合（C-03 の8章）。
	 * 確かめを偽物にして素通りさせ、データベースの重複禁止に当てる。
	 */
	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@Transactional
	@DisplayName("確かめのあとに、ほかの人が同じ管理番号を足していたとき")
	class Race {

		@MockitoBean
		ServiceInDtoChecker serviceInDtoChecker;

		@Test
		@DisplayName("重複禁止に当たる：400。errors の managementNo に「この管理番号はすでに使われています」")
		void duplicated() throws Exception {
			insert("A001", null);
			create(SOUMU, "{\"managementNo\":\"A001\",\"categoryId\":1}")
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errors.length()").value(1))
					.andExpect(jsonPath("$.errors[0].field").value("managementNo"))
					.andExpect(jsonPath("$.errors[0].message").value("この管理番号はすでに使われています"));
		}
	}

	// ここから下は、テストの道具

	ResultActions create(String email, String json) throws Exception {
		return mockMvc.perform(post("/api/equipment").with(login(email)).with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	RequestPostProcessor login(String email) {
		return authentication(new UsernamePasswordAuthenticationToken(new LoginUser(userId(email), LocalDateTime.now()),
				null, List.of()));
	}

	long userId(String email) {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
	}

	int count() {
		return jdbc.queryForObject("SELECT COUNT(*) FROM equipment", Integer.class);
	}

	void insert(String managementNo, String deletedAt) {
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, deleted_at, created_at, updated_at)"
				+ " VALUES (?, 1, 'MANAGED', " + (deletedAt == null ? "NULL" : deletedAt) + ", NOW(), NOW())",
				managementNo);
	}
}
