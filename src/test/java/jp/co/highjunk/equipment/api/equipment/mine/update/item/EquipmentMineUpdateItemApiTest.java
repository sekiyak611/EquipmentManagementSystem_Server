package jp.co.highjunk.equipment.api.equipment.mine.update.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import jp.co.highjunk.equipment.testsupport.CsrfTestHelper;
import jp.co.highjunk.equipment.testsupport.EquipmentMineApiTestBase;

/**
 * B-04 自分が借りている備品を「確認した」にする（API設計 3.2・4.7、詳細設計 equipment/mine/B-04_確認した.md）のテスト。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional。基底クラス）。今日は、アプリの時計（Clock）の今日。
 * 失敗するテストは、備品と貸出履歴の両方が、何も変わっていないことまで確かめる。
 * 同時に操作されたときの守りは、EquipmentMineUpdateItemLockTest。
 */
@DisplayName("B-04 自分が借りている備品を「確認した」にする")
class EquipmentMineUpdateItemApiTest extends EquipmentMineApiTestBase {

	static final String DENIED = "この操作を行う権限がありません。";

	static final String NOT_BORROWED = "借りていない備品が含まれているため、確認できません。";

	static final String NOT_MANAGED = "管理中の備品だけ確認できます";

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
		mockMvc.perform(post("/api/me/confirmations").with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content("{\"equipmentIds\":[1]}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("ログインしてください。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
	}

	@Test
	@DisplayName("CONFIRM_SELF がない：403（権限がない文）。備品も貸出履歴も変わらない")
	void noPermission() throws Exception {
		long loanId = ownLoan("K001", lastMonth);
		long roleId = insertRoleWithoutPermission();
		long userId = insertUser("nopermission@example.com", roleId);
		Map<String, Object> before = state(loanId);
		mockMvc.perform(post("/api/me/confirmations").with(loginAs(userId)).with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content(json(equipmentIdOf(loanId))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value(DENIED))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(loanId)).isEqualTo(before);
	}

	@Test
	@DisplayName("CSRF の合言葉がない：403（権限がない文）。何も変わらない")
	void noCsrfToken() throws Exception {
		long loanId = ownLoan("K002", lastMonth);
		Map<String, Object> before = state(loanId);
		mockMvc.perform(post("/api/me/confirmations").with(login(IPPAN)).contentType(MediaType.APPLICATION_JSON)
				.content(json(equipmentIdOf(loanId))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value(DENIED))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(loanId)).isEqualTo(before);
	}

	// ---------- 成功（3章・5章・7章） ----------

	@Test
	@DisplayName("1件確認する：200。備品の最終確認日が今日・version+1・更新日時・更新者。貸出履歴は変わらない（借りたまま）。一覧では、今月確認済みになり、未確認の数が減る")
	void confirmOne() throws Exception {
		long unconfirmed = ownLoan("K010", lastMonth);
		long other = ownLoan("K011", lastMonth);
		long equipmentId = equipmentIdOf(unconfirmed);
		Map<String, Object> before = state(unconfirmed);
		Map<String, Object> otherBefore = state(other);
		LocalDateTime start = LocalDateTime.now(clock).minusSeconds(2);

		confirm(IPPAN, equipmentId)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("処理が正常に完了しました。"))
				.andExpect(jsonPath("$.errors").value(nullValue()))
				.andExpect(jsonPath("$.data.unconfirmedCount").value(1))
				.andExpect(jsonPath("$.data.items", hasSize(2)))
				.andExpect(jsonPath("$.data.items[0].loanId").value(other))
				.andExpect(jsonPath("$.data.items[0].confirmedThisMonth").value(false))
				.andExpect(jsonPath("$.data.items[1].loanId").value(unconfirmed))
				.andExpect(jsonPath("$.data.items[1].equipmentId").value(equipmentId))
				.andExpect(jsonPath("$.data.items[1].managementNo").value("K010"))
				.andExpect(jsonPath("$.data.items[1].lastConfirmedOn").value(today.toString()))
				.andExpect(jsonPath("$.data.items[1].confirmedThisMonth").value(true))
				.andExpect(jsonPath("$.data.reminderAt").isString())
				.andExpect(jsonPath("$.data.reminderPassed").isBoolean());
		LocalDateTime end = LocalDateTime.now(clock).plusSeconds(2);

		Map<String, Object> after = state(unconfirmed);
		assertThat(after.get("last_confirmed_on").toString()).isEqualTo(today.toString());
		assertThat(intOf(after, "e_version")).isEqualTo(intOf(before, "e_version") + 1);
		assertThat(longOf(after, "e_updated_by")).isEqualTo(userId(IPPAN));
		assertThat(timeOf(after, "e_updated_at")).isBetween(start, end);
		assertThat(after.get("status")).isEqualTo("MANAGED");
		// 貸出履歴は、変わらない（返却日・メモ・version・更新者・更新日時）
		assertThat(loanColumns(after)).isEqualTo(loanColumns(before));
		assertThat(after.get("returned_on")).isNull();
		// 送らなかった、自分のほかの備品は、変わらない
		assertThat(state(other)).isEqualTo(otherBefore);
	}

	@Test
	@DisplayName("まとめて確認する：重なった ID は1つにまとめる（version は1つしか増えない）。今月すでに確認済みの備品が混ざっていても成功で、今日になる。未確認の数は 0 になる")
	void confirmMany() throws Exception {
		long l1 = ownLoan("K020", lastMonth);
		long l2 = ownLoan("K021", null);
		long l3 = loan(equipment("K022", "MANAGED", 1, null, null, today), userId(IPPAN), today.minusDays(2), null, null);
		List<Long> ids = List.of(equipmentIdOf(l3), equipmentIdOf(l1), equipmentIdOf(l2), equipmentIdOf(l1));
		Map<String, Object> b1 = state(l1);
		Map<String, Object> b3 = state(l3);

		postJson(IPPAN, json(ids.toArray(new Long[0])))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.unconfirmedCount").value(0))
				.andExpect(jsonPath("$.data.items", hasSize(3)))
				.andExpect(jsonPath("$.data.items[0].confirmedThisMonth").value(true))
				.andExpect(jsonPath("$.data.items[1].confirmedThisMonth").value(true))
				.andExpect(jsonPath("$.data.items[2].confirmedThisMonth").value(true));

		for (long loanId : List.of(l1, l2, l3)) {
			Map<String, Object> after = state(loanId);
			assertThat(after.get("last_confirmed_on").toString()).isEqualTo(today.toString());
			assertThat(after.get("returned_on")).isNull();
		}
		assertThat(intOf(state(l1), "e_version")).isEqualTo(intOf(b1, "e_version") + 1);
		assertThat(intOf(state(l3), "e_version")).isEqualTo(intOf(b3, "e_version") + 1);
		assertThat(intOf(state(l2), "e_version")).isEqualTo(1);
	}

	@Test
	@DisplayName("今月すでに確認済み（今月の1日に確認）の備品を送っても、誤りにならず、最終確認日が今日になる（12章 No.4。今日が1日のときは、確かめられないので飛ばす）")
	void alreadyConfirmedThisMonthIsUpdated() throws Exception {
		org.junit.jupiter.api.Assumptions.assumeTrue(today.getDayOfMonth() > 1);
		LocalDate firstOfMonth = today.withDayOfMonth(1);
		long loanId = ownLoan("K025", firstOfMonth);
		Map<String, Object> before = state(loanId);
		assertThat(before.get("last_confirmed_on").toString()).isEqualTo(firstOfMonth.toString());

		confirm(IPPAN, equipmentIdOf(loanId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items[0].lastConfirmedOn").value(today.toString()));
		Map<String, Object> after = state(loanId);
		assertThat(after.get("last_confirmed_on").toString()).isEqualTo(today.toString());
		assertThat(intOf(after, "e_version")).isEqualTo(intOf(before, "e_version") + 1);
	}

	@Test
	@DisplayName("日付を送っても使わない：最終確認日は今日になる")
	void dateIsIgnored() throws Exception {
		long loanId = ownLoan("K030", lastMonth);
		postJson(IPPAN, "{\"equipmentIds\":[" + equipmentIdOf(loanId) + "],\"lastConfirmedOn\":\"2020-01-01\"}")
				.andExpect(status().isOk());
		assertThat(state(loanId).get("last_confirmed_on").toString()).isEqualTo(today.toString());
	}

	@Test
	@DisplayName("確認しても、ほかの人の貸出・備品は変わらない。確認した本人の一覧にだけ出る")
	void othersAreUntouched() throws Exception {
		long mine = ownLoan("K031", lastMonth);
		long others = loan(equipment("K032", "MANAGED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(2),
				null, null);
		Map<String, Object> othersBefore = state(others);
		confirm(IPPAN, equipmentIdOf(mine)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items", hasSize(1)));
		assertThat(state(others)).isEqualTo(othersBefore);
	}

	@Test
	@DisplayName("総務も、自分が借りている分は確認できる")
	void soumuConfirmsOwn() throws Exception {
		long loanId = loan(equipment("K033", "MANAGED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(2),
				null, null);
		confirm(SOUMU, equipmentIdOf(loanId)).andExpect(status().isOk());
		assertThat(state(loanId).get("last_confirmed_on").toString()).isEqualTo(today.toString());
	}

	// ---------- 入力の誤り（9章 No.1・2） ----------

	@Test
	@DisplayName("equipmentIds が空の配列・ない・{}：400（equipmentIds 欄のエラー「確認する備品を選んでください」）")
	void emptyOrMissing() throws Exception {
		for (String body : new String[] { "{\"equipmentIds\":[]}", "{}", "{\"equipmentIds\":null}" }) {
			postJson(IPPAN, body)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
					.andExpect(jsonPath("$.data").value(nullValue()))
					.andExpect(jsonPath("$.errors", hasSize(1)))
					.andExpect(jsonPath("$.errors[0].field").value("equipmentIds"))
					.andExpect(jsonPath("$.errors[0].message").value("確認する備品を選んでください"));
		}
	}

	@Test
	@DisplayName("本文がない・JSON の形の誤り・中身が null・数でない：400（入力内容に誤りがあります。errors は null）。何も変わらない")
	void malformed() throws Exception {
		long loanId = ownLoan("K040", lastMonth);
		Map<String, Object> before = state(loanId);
		long id = equipmentIdOf(loanId);
		mockMvc.perform(post("/api/me/confirmations").with(login(IPPAN)).with(CsrfTestHelper.csrfToken(mockMvc)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		for (String body : new String[] { "{\"equipmentIds\":[" + id + ",null]}", "{\"equipmentIds\":[\"abc\"]}",
				"{\"equipmentIds\":" + id + "}", "[" + id + "]", "{\"equipmentIds\":[" + id + "" }) {
			postJson(IPPAN, body)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.message").value("入力内容に誤りがあります。"))
					.andExpect(jsonPath("$.data").value(nullValue()))
					.andExpect(jsonPath("$.errors").value(nullValue()));
		}
		assertThat(state(loanId)).isEqualTo(before);
	}

	// ---------- 対象の確かめ（9章 No.5〜7） ----------

	@Test
	@DisplayName("備品がない：404（対象が見つかりません）。送ったほかの備品も更新しない")
	void notFound() throws Exception {
		long loanId = ownLoan("K050", lastMonth);
		Map<String, Object> before = state(loanId);
		confirm(IPPAN, equipmentIdOf(loanId), 999_999_999L)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(loanId)).isEqualTo(before);
	}

	@Test
	@DisplayName("備品が削除されている（貸出中のまま）：404。ほかの備品も更新しない")
	void deletedEquipment() throws Exception {
		long ok = ownLoan("K051", lastMonth);
		long deleted = ownLoan("K052", lastMonth);
		jdbc.update("UPDATE equipment SET deleted_at = ? WHERE id = ?", LocalDateTime.now(clock), equipmentIdOf(deleted));
		Map<String, Object> before = state(ok);
		Map<String, Object> deletedBefore = state(deleted);
		confirm(IPPAN, equipmentIdOf(ok), equipmentIdOf(deleted)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"));
		assertThat(state(ok)).isEqualTo(before);
		assertThat(state(deleted)).isEqualTo(deletedBefore);
	}

	@Test
	@DisplayName("ほかの人が借りている備品：403（借りていない備品が含まれているため）。一緒に送った自分の備品も更新しない。その人の備品も変わらない")
	void othersEquipment() throws Exception {
		long mine = ownLoan("K060", lastMonth);
		long others = loan(equipment("K061", "MANAGED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(2),
				null, null);
		Map<String, Object> mineBefore = state(mine);
		Map<String, Object> othersBefore = state(others);

		confirm(IPPAN, equipmentIdOf(mine), equipmentIdOf(others))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value(NOT_BORROWED))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(mine)).isEqualTo(mineBefore);
		assertThat(state(others)).isEqualTo(othersBefore);

		// 総務が、一般社員の備品を確認しようとしても、この API では確認できない（C-06 の役目）
		confirm(SOUMU, equipmentIdOf(mine)).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(NOT_BORROWED));
		assertThat(state(mine)).isEqualTo(mineBefore);
	}

	@Test
	@DisplayName("借りられていない備品（保管中）：403。管理中でも、貸出履歴がなければ、確認できない")
	void notBorrowed() throws Exception {
		long free = equipment("K062", "MANAGED", 1, null, null, lastMonth);
		Object version = jdbc.queryForObject("SELECT version FROM equipment WHERE id = ?", Object.class, free);
		confirm(IPPAN, free).andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value(NOT_BORROWED));
		assertThat(jdbc.queryForObject("SELECT version FROM equipment WHERE id = ?", Object.class, free))
				.isEqualTo(version);
		assertThat(jdbc.queryForObject("SELECT last_confirmed_on FROM equipment WHERE id = ?", Object.class, free))
				.hasToString(lastMonth.toString());
	}

	@Test
	@DisplayName("返却済みの備品（別の端末で返した）：403。取り消された貸出の備品：403。何も変わらない")
	void returnedOrCancelled() throws Exception {
		long returned = loan(equipment("K063", "MANAGED", 1, null, null, lastMonth), userId(IPPAN), today.minusDays(5),
				null, today.minusDays(1));
		long cancelled = ownLoan("K064", lastMonth);
		jdbc.update("UPDATE loans SET deleted_at = ? WHERE id = ?", LocalDateTime.now(clock), cancelled);
		Map<String, Object> returnedBefore = state(returned);
		Map<String, Object> cancelledBefore = state(cancelled);

		confirm(IPPAN, equipmentIdOf(returned)).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(NOT_BORROWED));
		confirm(IPPAN, equipmentIdOf(cancelled)).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(NOT_BORROWED));
		assertThat(state(returned)).isEqualTo(returnedBefore);
		assertThat(state(cancelled)).isEqualTo(cancelledBefore);
	}

	@Test
	@DisplayName("B-03（返す）で返したあとの備品を、続けて確認しようとする：403。返したときの貸出履歴・備品は、そのまま")
	void afterReturn() throws Exception {
		long loanId = ownLoan("K065", lastMonth);
		long equipmentId = equipmentIdOf(loanId);
		mockMvc.perform(post("/api/me/loans/" + loanId + "/return").with(login(IPPAN))
				.with(CsrfTestHelper.csrfToken(mockMvc))).andExpect(status().isOk());
		Map<String, Object> afterReturn = state(loanId);

		confirm(IPPAN, equipmentId).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value(NOT_BORROWED))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(loanId)).isEqualTo(afterReturn);
	}

	@Test
	@DisplayName("管理中でない備品（廃棄・所在不明）：400（管理中の備品だけ確認できます）。一緒に送った管理中の備品も更新しない")
	void notManaged() throws Exception {
		long ok = ownLoan("K070", lastMonth);
		Map<String, Object> okBefore = state(ok);
		for (String status : new String[] { "DISPOSED", "MISSING" }) {
			long loanId = loan(equipment("K07" + status.charAt(0), status, 1, null, null, lastMonth), userId(IPPAN),
					today.minusDays(2), null, null);
			Map<String, Object> before = state(loanId);
			confirm(IPPAN, equipmentIdOf(ok), equipmentIdOf(loanId))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.message").value(NOT_MANAGED))
					.andExpect(jsonPath("$.data").value(nullValue()))
					.andExpect(jsonPath("$.errors").value(nullValue()));
			assertThat(state(loanId)).isEqualTo(before);
			assertThat(state(ok)).isEqualTo(okBefore);
		}
	}

	// ---------- 守れないものが複数あるときの順（common.md 3.20：404 → 403 → 400） ----------

	@Test
	@DisplayName("備品がない備品と、ほかの人の備品が混ざっている：404（404 を 403 より先に）")
	void notFoundBeforeForbidden() throws Exception {
		long others = loan(equipment("K080", "MANAGED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(2),
				null, null);
		Map<String, Object> before = state(others);
		confirm(IPPAN, 999_999_999L, equipmentIdOf(others)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(others)).isEqualTo(before);
	}

	@Test
	@DisplayName("備品がない備品と、自分の管理中でない備品が混ざっている：404（404 を 400 より先に）。3つ全部（備品がない・ほかの人の・管理中でない）が混ざっても 404")
	void notFoundBeforeBusinessRuleAndAll() throws Exception {
		long disposed = loan(equipment("K084", "DISPOSED", 1, null, null, lastMonth), userId(IPPAN), today.minusDays(2),
				null, null);
		long others = loan(equipment("K085", "MANAGED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(2),
				null, null);
		Map<String, Object> disposedBefore = state(disposed);
		confirm(IPPAN, 999_999_999L, equipmentIdOf(disposed)).andExpect(status().isNotFound());
		confirm(IPPAN, 999_999_999L, equipmentIdOf(disposed), equipmentIdOf(others)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("対象が見つかりません。"));
		assertThat(state(disposed)).isEqualTo(disposedBefore);
	}

	@Test
	@DisplayName("ほかの人の備品と、自分の管理中でない備品が混ざっている：403（403 を 400 より先に）")
	void forbiddenBeforeBusinessRule() throws Exception {
		long others = loan(equipment("K081", "MANAGED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(2),
				null, null);
		long disposed = loan(equipment("K082", "DISPOSED", 1, null, null, lastMonth), userId(IPPAN), today.minusDays(2),
				null, null);
		Map<String, Object> othersBefore = state(others);
		Map<String, Object> disposedBefore = state(disposed);
		confirm(IPPAN, equipmentIdOf(disposed), equipmentIdOf(others)).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value(NOT_BORROWED))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(others)).isEqualTo(othersBefore);
		assertThat(state(disposed)).isEqualTo(disposedBefore);
	}

	@Test
	@DisplayName("ほかの人の、管理中でない備品：403（借りていない備品として）")
	void othersDisposed() throws Exception {
		long others = loan(equipment("K083", "DISPOSED", 1, null, null, lastMonth), userId(SOUMU), today.minusDays(2),
				null, null);
		Map<String, Object> before = state(others);
		confirm(IPPAN, equipmentIdOf(others)).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(NOT_BORROWED))
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").value(nullValue()));
		assertThat(state(others)).isEqualTo(before);
	}

	// ---------- 道具 ----------

	ResultActions postJson(String email, String json) throws Exception {
		return mockMvc.perform(post("/api/me/confirmations").with(login(email)).with(CsrfTestHelper.csrfToken(mockMvc))
				.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	ResultActions confirm(String email, Long... equipmentIds) throws Exception {
		return postJson(email, json(equipmentIds));
	}

	static String json(Long... equipmentIds) {
		StringBuilder b = new StringBuilder("{\"equipmentIds\":[");
		for (int i = 0; i < equipmentIds.length; i++) {
			b.append(i == 0 ? "" : ",").append(equipmentIds[i]);
		}
		return b.append("]}").toString();
	}

	long equipmentIdOf(long loanId) {
		return jdbc.queryForObject("SELECT equipment_id FROM loans WHERE id = ?", Long.class, loanId);
	}

	/** 貸出履歴と、その備品の、変わるかもしれない列をすべて読む（失敗したときに、何も変わっていないことを確かめるため） */
	Map<String, Object> state(long loanId) {
		return jdbc.queryForMap("SELECT l.returned_on, l.memo, l.version, l.updated_by, l.updated_at,"
				+ " l.active_equipment_id, l.deleted_at, e.last_confirmed_on, e.version AS e_version,"
				+ " e.updated_by AS e_updated_by, e.updated_at AS e_updated_at, e.status, e.deleted_at AS e_deleted_at"
				+ " FROM loans l JOIN equipment e ON e.id = l.equipment_id WHERE l.id = ?", loanId);
	}

	/** state のうち、貸出履歴の列だけ */
	static Map<String, Object> loanColumns(Map<String, Object> state) {
		Map<String, Object> copy = new java.util.HashMap<>();
		for (String key : List.of("returned_on", "memo", "version", "updated_by", "updated_at", "active_equipment_id",
				"deleted_at")) {
			copy.put(key, state.get(key));
		}
		return copy;
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

	/** 一般社員が借りている、管理中の備品の貸出履歴を1つ入れ、ID を返す */
	long ownLoan(String managementNo, LocalDate lastConfirmedOn) {
		return loan(equipment(managementNo, "MANAGED", 1, "テスト", "T-1", lastConfirmedOn), userId(IPPAN),
				today.minusDays(3), today.plusDays(10), null);
	}
}
