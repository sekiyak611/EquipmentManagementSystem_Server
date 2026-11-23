package jp.co.highjunk.equipment.api.equipment.mine.update.returnitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;
import jp.co.highjunk.equipment.testsupport.EquipmentMineApiTestBase;

/**
 * B-03 備品を返す（API設計 3.2・4.5・4.7、詳細設計 equipment/mine/B-03_備品を返す.md）のテスト。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional。基底クラス）。今日は、アプリの時計（Clock）の今日。
 * 失敗するテストは、貸出履歴と備品の両方が、何も変わっていないことまで確かめる。
 * 同時に操作されたときの守りは、EquipmentMineUpdateReturnItemLockTest。
 */
@DisplayName("B-03 備品を返す")
class EquipmentMineUpdateReturnItemApiTest extends EquipmentMineApiTestBase {

	static final String DENIED = "この操作を行う権限がありません。";

	static final String FORBIDDEN_OTHER = "ほかの人が借りているため、返却はできません。";

	static final String ALREADY_RETURNED = "この備品はすでに返却されています";

	static final String NOT_MANAGED = "この備品は返却できません。総務へお問い合わせください。";

	LocalDate today;

	LocalDate lastMonth;

	@BeforeEach
	void setUp() throws Exception {
		waitIfNearMidnight();
		today = LocalDate.now(clock);
		lastMonth = today.withDayOfMonth(1).minusDays(1);
	}

	// ---------- ログイン・権限（9章 No.3・4） ----------

	@Test
	@DisplayName("ログインしていない：401（ログインしてください）")
	void notLoggedIn() throws Exception {
		mockMvc.perform(post("/api/me/loans/1/return").with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("ログインしてください。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@DisplayName("LOAN_SELF がない：403（権限がない文）。貸出履歴も備品も変わらない")
	void noPermission() throws Exception {
		long loanId = ownLoan("R001", lastMonth);
		long roleId = insertRoleWithoutPermission();
		long userId = insertUser("nopermission@example.com", roleId);
		Map<String, Object> before = state(loanId);
		mockMvc.perform(post(url(loanId)).with(loginAs(userId)).with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value(DENIED))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(loanId)).isEqualTo(before);
	}

	@Test
	@DisplayName("CSRF の合言葉がない：403（権限がない文）。貸出履歴も備品も変わらない")
	void noCsrfToken() throws Exception {
		long loanId = ownLoan("R002", lastMonth);
		Map<String, Object> before = state(loanId);
		mockMvc.perform(post(url(loanId)).with(login(IPPAN)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(DENIED));
		assertThat(state(loanId)).isEqualTo(before);
	}

	// ---------- 成功（3章・5章・7章） ----------

	@Test
	@DisplayName("返す：200。貸出履歴の返却日が今日・メモ・version+1・更新日時・更新者、備品の最終確認日が今日・version+1・更新日時・更新者。返した備品は一覧から消える")
	void returnWithMemo() throws Exception {
		long equipmentId = equipment("R010", "MANAGED", 1, "テスト", "T-1", lastMonth);
		long loanId = loan(equipmentId, userId(IPPAN), today.minusDays(3), today.plusDays(10), null);
		Map<String, Object> before = state(loanId);
		LocalDateTime start = LocalDateTime.now(clock).minusSeconds(2);

		returnItem(IPPAN, loanId, "{\"memo\":\"  画面に小さなひびあり  \"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.errors").value(nullValue()))
				.andExpect(jsonPath("$.data.unconfirmedCount").value(0))
				.andExpect(jsonPath("$.data.items", hasSize(0)))
				.andExpect(jsonPath("$.data.reminderAt").isString())
				.andExpect(jsonPath("$.data.reminderPassed").isBoolean());
		LocalDateTime end = LocalDateTime.now(clock).plusSeconds(2);

		Map<String, Object> after = state(loanId);
		assertThat(after.get("returned_on").toString()).isEqualTo(today.toString());
		assertThat(after.get("memo")).isEqualTo("画面に小さなひびあり");
		assertThat(intOf(after, "version")).isEqualTo(intOf(before, "version") + 1);
		assertThat(longOf(after, "updated_by")).isEqualTo(userId(IPPAN));
		assertThat(timeOf(after, "updated_at")).isBetween(start, end);
		assertThat(after.get("active_equipment_id")).isNull();
		assertThat(after.get("last_confirmed_on").toString()).isEqualTo(today.toString());
		assertThat(intOf(after, "e_version")).isEqualTo(intOf(before, "e_version") + 1);
		assertThat(longOf(after, "e_updated_by")).isEqualTo(userId(IPPAN));
		assertThat(timeOf(after, "e_updated_at")).isBetween(start, end);
		assertThat(after.get("status")).isEqualTo("MANAGED");
	}

	@Test
	@DisplayName("本文なし・{} ・メモなしでも返せる。メモを書かなければ、貸出のときのメモは変わらない")
	void returnWithoutMemo() throws Exception {
		long loanNoBody = loan(equipment("R020", "MANAGED", 1, null, null, lastMonth), userId(IPPAN),
				today.minusDays(1), null, null, "貸出のときのメモ");
		mockMvc.perform(post(url(loanNoBody)).with(login(IPPAN)).with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isOk());
		assertThat(returnedOn(loanNoBody)).isEqualTo(today.toString());
		assertThat(memo(loanNoBody)).isEqualTo("貸出のときのメモ");

		long loanEmptyJson = loan(equipment("R021", "MANAGED", 1, null, null, lastMonth), userId(IPPAN),
				today.minusDays(1), null, null);
		returnItem(IPPAN, loanEmptyJson, "{}").andExpect(status().isOk());
		assertThat(returnedOn(loanEmptyJson)).isEqualTo(today.toString());
		assertThat(memo(loanEmptyJson)).isNull();
	}

	@Test
	@DisplayName("返却時のメモは、貸出のときのメモを上書きする")
	void memoOverwrites() throws Exception {
		long loanId = loan(equipment("R022", "MANAGED", 1, null, null, lastMonth), userId(IPPAN), today.minusDays(1),
				null, null, "貸出のときのメモ");
		returnItem(IPPAN, loanId, "{\"memo\":\"返却のときのメモ\"}").andExpect(status().isOk());
		assertThat(memo(loanId)).isEqualTo("返却のときのメモ");
	}

	@Test
	@DisplayName("メモが空白だけ・空：記録しない。貸出のときのメモがなければ null、あれば、そのまま残る（上書きも消しもしない）")
	void blankMemoIsNotRecorded() throws Exception {
		long blank = ownLoan("R023", lastMonth);
		returnItem(IPPAN, blank, "{\"memo\":\"   　 \"}").andExpect(status().isOk());
		assertThat(memo(blank)).isNull();

		long empty = ownLoan("R024", lastMonth);
		returnItem(IPPAN, empty, "{\"memo\":\"\"}").andExpect(status().isOk());
		assertThat(memo(empty)).isNull();

		long withOld = loan(equipment("R026", "MANAGED", 1, null, null, lastMonth), userId(IPPAN), today.minusDays(1),
				null, null, "貸出のときのメモ");
		returnItem(IPPAN, withOld, "{\"memo\":\"   \"}").andExpect(status().isOk());
		assertThat(returnedOn(withOld)).isEqualTo(today.toString());
		assertThat(memo(withOld)).isEqualTo("貸出のときのメモ");
	}

	@Test
	@DisplayName("返却日を送っても使わない：今日になる")
	void returnedOnIsIgnored() throws Exception {
		long loanId = ownLoan("R025", lastMonth);
		returnItem(IPPAN, loanId, "{\"returnedOn\":\"2020-01-01\",\"memo\":\"確認\"}").andExpect(status().isOk());
		assertThat(returnedOn(loanId)).isEqualTo(today.toString());
	}

	@Test
	@DisplayName("借りた日に返す（貸出日が今日、返却日も今日）も、期限を過ぎた貸出も、返せる")
	void boundaryDates() throws Exception {
		long sameDay = loan(equipment("R027", "MANAGED", 1, null, null, lastMonth), userId(IPPAN), today,
				today.plusDays(10), null);
		returnItem(IPPAN, sameDay, "{}").andExpect(status().isOk());
		assertThat(returnedOn(sameDay)).isEqualTo(today.toString());

		long overdue = loan(equipment("R028", "MANAGED", 1, null, null, lastMonth), userId(IPPAN), today.minusDays(30),
				today.minusDays(1), null);
		returnItem(IPPAN, overdue, "{}").andExpect(status().isOk());
		assertThat(returnedOn(overdue)).isEqualTo(today.toString());
	}

	@Test
	@DisplayName("返したあとの一覧：返した備品は出ず、ほかに借りている備品が残る。今月まだ確認していない数は、返した分だけ減る")
	void listAfterReturn() throws Exception {
		long returning = ownLoan("R030", lastMonth);
		long staying = loan(equipment("R031", "MANAGED", 1, null, null, lastMonth), userId(IPPAN), today.minusDays(5),
				null, null);
		long confirmed = loan(equipment("R032", "MANAGED", 1, null, null, today), userId(IPPAN), today.minusDays(4),
				null, null);

		returnItem(IPPAN, returning, "{}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.unconfirmedCount").value(1))
				.andExpect(jsonPath("$.data.items", hasSize(2)))
				.andExpect(jsonPath("$.data.items[0].loanId").value(staying))
				.andExpect(jsonPath("$.data.items[0].managementNo").value("R031"))
				.andExpect(jsonPath("$.data.items[0].confirmedThisMonth").value(false))
				.andExpect(jsonPath("$.data.items[1].loanId").value(confirmed))
				.andExpect(jsonPath("$.data.items[1].confirmedThisMonth").value(true));
	}

	@Test
	@DisplayName("返した備品は、また借りられる（返却日が入ると、貸出中の印が外れる）")
	void canBeLentAgain() throws Exception {
		long equipmentId = equipment("R033", "MANAGED", 1, null, null, lastMonth);
		long loanId = loan(equipmentId, userId(IPPAN), today.minusDays(2), null, null);
		returnItem(IPPAN, loanId, "{}").andExpect(status().isOk());

		LocalDateTime now = LocalDateTime.now(clock);
		int inserted = jdbc.update("INSERT INTO loans (equipment_id, user_id, lent_on, created_at, updated_at)"
				+ " VALUES (?, ?, ?, ?, ?)", equipmentId, userId(SOUMU), today, now, now);
		assertThat(inserted).isEqualTo(1);
	}

	@Test
	@DisplayName("総務も、自分が借りている分は返せる")
	void soumuReturnsOwn() throws Exception {
		long loanId = loan(equipment("R034", "MANAGED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(2),
				null, null);
		returnItem(SOUMU, loanId, "{}").andExpect(status().isOk());
		assertThat(returnedOn(loanId)).isEqualTo(today.toString());
	}

	// ---------- 入力の誤り（9章 No.1・2） ----------

	@Test
	@DisplayName("メモ：200文字ちょうどは通る（前後の空白は数えない）。201文字は 400（メモ欄のエラー）で、何も変わらない")
	void memoLength() throws Exception {
		long ok = ownLoan("R040", lastMonth);
		returnItem(IPPAN, ok, "{\"memo\":\"  " + "あ".repeat(200) + "  \"}").andExpect(status().isOk());
		assertThat(memo(ok)).isEqualTo("あ".repeat(200));

		long ng = ownLoan("R041", lastMonth);
		Map<String, Object> before = state(ng);
		returnItem(IPPAN, ng, "{\"memo\":\"" + "あ".repeat(201) + "\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("memo"))
				.andExpect(jsonPath("$.errors[0].message").value("メモは200文字以内で入力してください"));
		assertThat(state(ng)).isEqualTo(before);
	}

	@Test
	@DisplayName("貸出履歴のID が数でない：400（入力内容に誤りがあります）")
	void loanIdNotNumber() throws Exception {
		mockMvc.perform(post("/api/me/loans/abc/return").with(login(IPPAN)).with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	// ---------- 対象の確かめ（9章 No.5〜8） ----------

	@Test
	@DisplayName("貸出履歴がない：404（対象が見つかりません）")
	void notFound() throws Exception {
		returnItem(IPPAN, 999_999_999L, "{}")
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@DisplayName("取り消された貸出履歴：404。貸出履歴も備品も変わらない")
	void cancelledLoan() throws Exception {
		long loanId = ownLoan("R050", lastMonth);
		jdbc.update("UPDATE loans SET deleted_at = ? WHERE id = ?", LocalDateTime.now(clock), loanId);
		Map<String, Object> before = state(loanId);
		returnItem(IPPAN, loanId, "{}").andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"));
		assertThat(state(loanId)).isEqualTo(before);
	}

	@Test
	@DisplayName("ほかの人が借りている貸出履歴：403（文はほかの人が借りているため）。その人の貸出も備品も変わらない。総務でも同じ")
	void othersLoan() throws Exception {
		long loanId = loan(equipment("R051", "MANAGED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(2),
				null, null);
		Map<String, Object> before = state(loanId);

		returnItem(IPPAN, loanId, "{\"memo\":\"他人の分\"}")
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value(FORBIDDEN_OTHER))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(loanId)).isEqualTo(before);

		// 総務が、一般社員の貸出履歴を返そうとしても、この API では返せない（C-08 の役目）
		long ippanLoan = ownLoan("R052", lastMonth);
		Map<String, Object> ippanBefore = state(ippanLoan);
		returnItem(SOUMU, ippanLoan, "{}").andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(FORBIDDEN_OTHER));
		assertThat(state(ippanLoan)).isEqualTo(ippanBefore);
	}

	@Test
	@DisplayName("すでに返却されている：409（この備品はすでに返却されています）。貸出履歴も備品も変わらない（返却日・メモ・version・最終確認日）")
	void alreadyReturned() throws Exception {
		LocalDate returned = today.minusDays(1);
		long loanId = loan(equipment("R053", "MANAGED", 1, null, null, lastMonth), userId(IPPAN), today.minusDays(5),
				null, returned, "先に返したときのメモ");
		Map<String, Object> before = state(loanId);

		returnItem(IPPAN, loanId, "{\"memo\":\"二度目\"}")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").value(ALREADY_RETURNED))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(loanId)).isEqualTo(before);
		assertThat(returnedOn(loanId)).isEqualTo(returned.toString());
		assertThat(memo(loanId)).isEqualTo("先に返したときのメモ");
	}

	@Test
	@DisplayName("同じ貸出履歴を2回続けて返す：1回目は 200、2回目は 409。2回目では何も変わらない")
	void returnTwice() throws Exception {
		long loanId = ownLoan("R054", lastMonth);
		returnItem(IPPAN, loanId, "{}").andExpect(status().isOk());
		Map<String, Object> afterFirst = state(loanId);
		returnItem(IPPAN, loanId, "{\"memo\":\"二度目\"}").andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(ALREADY_RETURNED));
		assertThat(state(loanId)).isEqualTo(afterFirst);
		assertThat(returnedOn(loanId)).isEqualTo(today.toString());
	}

	@Test
	@DisplayName("備品が管理中でない（廃棄・所在不明）：400（総務へお問い合わせください）。貸出履歴も備品も変わらない")
	void equipmentNotManaged() throws Exception {
		for (String status : new String[] { "DISPOSED", "MISSING" }) {
			long equipmentId = equipment("R06" + status.charAt(0), status, 1, null, null, lastMonth);
			long loanId = loan(equipmentId, userId(IPPAN), today.minusDays(2), null, null);
			Map<String, Object> before = state(loanId);

			returnItem(IPPAN, loanId, "{}")
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.message").value(NOT_MANAGED))
					.andExpect(jsonPath("$.data").value(nullValue()))
					.andExpect(jsonPath("$.errors").value(nullValue()));
			assertThat(state(loanId)).isEqualTo(before);
			assertThat(before.get("status")).isEqualTo(status);
		}
	}

	@Test
	@DisplayName("備品が削除されている：管理中でないものとして 400。貸出履歴は変わらない")
	void equipmentDeleted() throws Exception {
		long equipmentId = equipment("R070", "MANAGED", 1, null, null, lastMonth);
		long loanId = loan(equipmentId, userId(IPPAN), today.minusDays(2), null, null);
		jdbc.update("UPDATE equipment SET deleted_at = ? WHERE id = ?", LocalDateTime.now(clock), equipmentId);
		Map<String, Object> before = state(loanId);
		returnItem(IPPAN, loanId, "{}").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(NOT_MANAGED));
		assertThat(state(loanId)).isEqualTo(before);
	}

	// ---------- 守れないものが複数あるときの順（common.md 3.20：404 → 403 → 409 → 400） ----------

	@Test
	@DisplayName("取り消された貸出履歴は、ほかの人のものでも、返却済みでも、404（404 を 403・409 より先に）")
	void notFoundFirst() throws Exception {
		long othersCancelled = loan(equipment("R090", "DISPOSED", 1, null, null, lastMonth), userId(SOUMU),
				today.minusDays(5), null, null);
		jdbc.update("UPDATE loans SET deleted_at = ? WHERE id = ?", LocalDateTime.now(clock), othersCancelled);
		returnItem(IPPAN, othersCancelled, "{}").andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"));

		long returnedCancelled = loan(equipment("R091", "MANAGED", 1, null, null, lastMonth), userId(IPPAN),
				today.minusDays(5), null, today.minusDays(1));
		jdbc.update("UPDATE loans SET deleted_at = ? WHERE id = ?", LocalDateTime.now(clock), returnedCancelled);
		returnItem(IPPAN, returnedCancelled, "{}").andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("ほかの人が借りていて、しかも返却済み：403（403 を 409 より先に）")
	void forbiddenBeforeConflict() throws Exception {
		long loanId = loan(equipment("R080", "MANAGED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(5),
				null, today.minusDays(1));
		returnItem(IPPAN, loanId, "{}").andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(FORBIDDEN_OTHER));
	}

	@Test
	@DisplayName("ほかの人が借りていて、しかも備品が管理中でない：403（403 を 400 より先に）")
	void forbiddenBeforeBusinessRule() throws Exception {
		long loanId = loan(equipment("R081", "DISPOSED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(5),
				null, null);
		returnItem(IPPAN, loanId, "{}").andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(FORBIDDEN_OTHER));
	}

	@Test
	@DisplayName("返却済みで、しかも備品が管理中でない：409（409 を 400 より先に）")
	void conflictBeforeBusinessRule() throws Exception {
		long loanId = loan(equipment("R082", "MISSING", 1, null, null, lastMonth), userId(IPPAN), today.minusDays(5),
				null, today.minusDays(1));
		returnItem(IPPAN, loanId, "{}").andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(ALREADY_RETURNED));
	}

	@Test
	@DisplayName("ほかの人が借りていて、返却済みで、しかも備品が管理中でない（3つとも）：403")
	void allThreeBroken() throws Exception {
		long loanId = loan(equipment("R083", "DISPOSED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(5),
				null, today.minusDays(1));
		returnItem(IPPAN, loanId, "{}").andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(FORBIDDEN_OTHER));
	}

	// ---------- 道具 ----------

	String url(long loanId) {
		return "/api/me/loans/" + loanId + "/return";
	}

	ResultActions returnItem(String email, long loanId, String json) throws Exception {
		return mockMvc.perform(post(url(loanId)).with(login(email)).with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	/** 貸出履歴と、その備品の、変わるかもしれない列をすべて読む（失敗したときに、何も変わっていないことを確かめるため） */
	Map<String, Object> state(long loanId) {
		return jdbc.queryForMap("SELECT l.returned_on, l.memo, l.version, l.updated_by, l.updated_at,"
				+ " l.active_equipment_id, l.deleted_at, e.last_confirmed_on, e.version AS e_version,"
				+ " e.updated_by AS e_updated_by, e.updated_at AS e_updated_at, e.status, e.deleted_at AS e_deleted_at"
				+ " FROM loans l JOIN equipment e ON e.id = l.equipment_id WHERE l.id = ?", loanId);
	}

	static int intOf(Map<String, Object> row, String key) {
		return ((Number) row.get(key)).intValue();
	}

	static long longOf(Map<String, Object> row, String key) {
		return ((Number) row.get(key)).longValue();
	}

	static LocalDateTime timeOf(Map<String, Object> row, String key) {
		Object value = row.get(key);
		return value instanceof LocalDateTime time ? time : ((Timestamp) value).toLocalDateTime();
	}

	String returnedOn(long loanId) {
		Object value = jdbc.queryForObject("SELECT returned_on FROM loans WHERE id = ?", Object.class, loanId);
		return value == null ? null : value.toString();
	}

	String memo(long loanId) {
		return jdbc.queryForObject("SELECT memo FROM loans WHERE id = ?", String.class, loanId);
	}

	/** 一般社員が借りている、管理中の備品の貸出履歴を1つ入れ、ID を返す */
	long ownLoan(String managementNo, LocalDate lastConfirmedOn) {
		return loan(equipment(managementNo, "MANAGED", 1, "テスト", "T-1", lastConfirmedOn), userId(IPPAN),
				today.minusDays(3), today.plusDays(10), null);
	}
}
