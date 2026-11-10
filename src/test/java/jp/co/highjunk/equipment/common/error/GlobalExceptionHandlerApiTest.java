package jp.co.highjunk.equipment.common.error;

import static jp.co.highjunk.equipment.testsupport.CsrfTestHelper.csrfToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * エラーのときに docs/API設計.md 2.4 の形で返すことのテスト。
 * テストの中だけにある ErrorTestController で、わざとエラーを起こして確かめる。
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class GlobalExceptionHandlerApiTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("入力の誤り：400。errors に入力欄ごとのメッセージ")
	void validationError() throws Exception {
		mockMvc.perform(post("/api/test-errors/validation").with(csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content("{\"userName\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.errors[0].field").value("userName"))
				.andExpect(jsonPath("$.errors[0].message").value("ユーザー名は必須です。"))
				.andExpect(jsonPath("$.errors[0].row").doesNotExist());
	}

	@Test
	@DisplayName("正常時：200。status・message・data・errors の形")
	void success() throws Exception {
		mockMvc.perform(post("/api/test-errors/validation").with(csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content("{\"userName\":\"山田太郎\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.data").value("山田太郎"))
				.andExpect(jsonPath("$.errors").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	@DisplayName("JSONの形が壊れている：400。errors は null")
	void brokenJson() throws Exception {
		mockMvc.perform(post("/api/test-errors/validation").with(csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content("{壊れたJSON"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.errors").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	@DisplayName("数字の欄に文字が入っている：400")
	void typeMismatch() throws Exception {
		mockMvc.perform(get("/api/test-errors/page").param("page", "abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"));
	}

	@Test
	@DisplayName("決まりに合わない操作：400。message に各機能が渡した文")
	void businessRule() throws Exception {
		mockMvc.perform(get("/api/test-errors/business-rule"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("貸出中の備品は削除できません"))
				.andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.errors").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	@DisplayName("見つからない：404")
	void notFound() throws Exception {
		mockMvc.perform(get("/api/test-errors/not-found"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"));
	}

	@Test
	@DisplayName("存在しないアドレス：404")
	void unknownPath() throws Exception {
		mockMvc.perform(get("/api/no-such-api"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"));
	}

	@Test
	@DisplayName("ぶつかった：409。message に各機能が渡した文")
	void conflict() throws Exception {
		mockMvc.perform(get("/api/test-errors/conflict"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").value("この備品はすでに貸し出されています"));
	}

	@Test
	@DisplayName("業務チェックの入力欄のエラー（FieldErrorException）：400。入力の誤りと同じ文で、errors に入力欄ごとの文。data は null。行番号・管理番号の項目は出さない")
	void fieldError() throws Exception {
		mockMvc.perform(get("/api/test-errors/field-error"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.errors.length()").value(2))
				.andExpect(jsonPath("$.errors[0].field").value("name"))
				.andExpect(jsonPath("$.errors[0].message").value("この名前はすでに使われています"))
				.andExpect(jsonPath("$.errors[0].row").doesNotExist())
				.andExpect(jsonPath("$.errors[0].managementNo").doesNotExist())
				.andExpect(jsonPath("$.errors[1].field").value("initial"))
				.andExpect(jsonPath("$.errors[1].message").value("この頭文字はすでに使われています"));
	}

	@Test
	@DisplayName("CSVの行のエラー：400。errors に行番号・管理番号・理由")
	void csvError() throws Exception {
		mockMvc.perform(get("/api/test-errors/csv"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("エラーがあったため、更新していません。直してからもう一度アップロードしてください。"))
				.andExpect(jsonPath("$.errors[0].field").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.errors[0].row").value(12))
				.andExpect(jsonPath("$.errors[0].managementNo").value("D003"))
				.andExpect(jsonPath("$.errors[0].message").value("不明の備品です。"));
	}

	@Test
	@DisplayName("思いがけない不具合：500。詳しい中身は返さない")
	void unexpected() throws Exception {
		mockMvc.perform(get("/api/test-errors/unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.message")
						.value("問題が発生しました。時間をおいて、もう一度お試しください。続く場合は総務に連絡してください。"))
				.andExpect(content().string(not(containsString("画面に出してはいけない詳しい中身"))));
	}

	@Test
	@DisplayName("必要な権限項目がない：403")
	void forbidden() throws Exception {
		mockMvc.perform(get("/api/test-errors/permission"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value("この操作を行う権限がありません。"))
				.andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	@WithMockUser(authorities = "EQUIPMENT_UPDATE")
	@DisplayName("必要な権限項目がある：200")
	void permitted() throws Exception {
		mockMvc.perform(get("/api/test-errors/permission"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value("ok"));
	}
}
