package jp.co.highjunk.equipment.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import jp.co.highjunk.equipment.testsupport.AuthApiTestBase;

/**
 * APIを呼ぶたびの権限項目の読み直し（docs/API設計.md 2.2、詳細設計 auth/auth.md、common.md 3.10）のテスト。
 */
@DisplayName("APIを呼ぶたびの読み直し（API設計 2.2）")
class LoginUserRefresherApiTest extends AuthApiTestBase {

	@Test
	@DisplayName("権限の変更は、ログインし直さなくても次の操作から効く")
	void permissionChangeTakesEffect() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		mockMvc.perform(get("/api/test-errors/permission").session(session))
				.andExpect(status().isForbidden());

		change("INSERT INTO role_permissions (role_id, permission_code) VALUES (?, 'EQUIPMENT_UPDATE')",
				roleId(IPPAN));
		change("INSERT INTO role_permissions (role_id, permission_code) VALUES (?, 'EQUIPMENT_VIEW')",
				roleId(IPPAN));

		mockMvc.perform(get("/api/test-errors/permission").session(session))
				.andExpect(status().isOk());
		me(session)
				.andExpect(jsonPath("$.data.permissions",
						contains("EQUIPMENT_VIEW", "EQUIPMENT_UPDATE", "LOAN_SELF", "CONFIRM_SELF")))
				.andExpect(jsonPath("$.data.homePage").value("EQUIPMENT_LIST"));
	}

	@Test
	@DisplayName("ログインしている人が削除されたら、次の操作で 401。セッションも捨てる")
	void deletedAfterLogin() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		change("UPDATE users SET deleted_at = " + NOW + " WHERE email = ?", IPPAN);

		me(session).andExpect(status().isUnauthorized());
		assertThat(session.isInvalid()).isTrue();
	}

	@Test
	@DisplayName("ログインのあとにパスワードが変わった（session_valid_after がログインより後）なら、次の操作で 401")
	void passwordChangedAfterLogin() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		change("UPDATE users SET session_valid_after = '2999-01-01 00:00:00' WHERE email = ?", IPPAN);

		me(session).andExpect(status().isUnauthorized());
		assertThat(session.isInvalid()).isTrue();
	}

	@Test
	@DisplayName("session_valid_after がログインより前なら、そのまま使える")
	void passwordChangedBeforeLogin() throws Exception {
		change("UPDATE users SET session_valid_after = '2000-01-01 00:00:00' WHERE email = ?", IPPAN);
		MockHttpSession session = loginSession(IPPAN);

		me(session).andExpect(status().isOk());
	}

	@Test
	@DisplayName("ロックされても、ログインしている人は締め出さない（画面設計 2.1 の6）")
	void lockedAfterLogin() throws Exception {
		MockHttpSession session = loginSession(IPPAN);
		change("UPDATE users SET locked_at = " + NOW + " WHERE email = ?", IPPAN);

		me(session).andExpect(status().isOk());
	}
}
