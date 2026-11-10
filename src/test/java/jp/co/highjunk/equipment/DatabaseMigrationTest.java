package jp.co.highjunk.equipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * テーブルと最初のデータ（ZPOP-122。docs/データ設計.md 2〜7章、詳細設計 テーブル.md）のテスト。
 * テスト専用のデータベース（equipment_test）に、Flyway が V1・V2 を当てたあとの形を確かめる。
 * 書き込むテストは、終わるたびに元に戻す（@Transactional。docs/テスト方針.md 4章）。
 */
@SpringBootTest
@Transactional
class DatabaseMigrationTest {

	private static final String NOW = "'2026-11-08 10:00:00'";

	@Autowired
	JdbcTemplate jdbc;

	@Nested
	@DisplayName("テーブル（データ設計 2・3章）")
	class Tables {

		@Test
		@DisplayName("データ設計 3章の13個のテーブルがあり、どれも照合順序が utf8mb4_ja_0900_as_cs_ks")
		void allTablesWithJapaneseCollation() {
			List<Map<String, Object>> rows = jdbc.queryForList("""
					SELECT TABLE_NAME, TABLE_COLLATION FROM information_schema.TABLES
					WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME <> 'flyway_schema_history'
					ORDER BY TABLE_NAME""");

			assertThat(rows).extracting(r -> r.get("TABLE_NAME")).containsExactly(
					"categories", "equipment", "holidays", "loans", "password_tokens", "permissions",
					"persistent_logins", "reminder_log_items", "reminder_logs", "reminder_runs",
					"role_permissions", "roles", "users");
			assertThat(rows).extracting(r -> r.get("TABLE_COLLATION")).containsOnly("utf8mb4_ja_0900_as_cs_ks");
		}

		@Test
		@DisplayName("データ設計 6章の索引が、決めた列に、決めた順で付いている")
		void indexes() {
			List<String> indexes = jdbc.queryForList("""
					SELECT CONCAT(TABLE_NAME, '(', GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX), ')')
					FROM information_schema.STATISTICS
					WHERE TABLE_SCHEMA = DATABASE() AND INDEX_NAME LIKE 'idx\\_%'
					GROUP BY TABLE_NAME, INDEX_NAME""", String.class);

			assertThat(indexes).containsExactlyInAnyOrder(
					"equipment(status,last_confirmed_on)",
					"equipment(category_id)",
					"loans(equipment_id,lent_on)",
					"loans(user_id,returned_on)",
					"password_tokens(user_id,created_at)",
					"reminder_logs(run_id)",
					"users(role_id)");
		}

		@Test
		@DisplayName("データ設計 5章で「〜を指す」と書いた11の列に、外部キーが付いている")
		void foreignKeys() {
			List<String> keys = jdbc.queryForList("""
					SELECT CONCAT(TABLE_NAME, '.', COLUMN_NAME, '->', REFERENCED_TABLE_NAME, '.', REFERENCED_COLUMN_NAME)
					FROM information_schema.KEY_COLUMN_USAGE
					WHERE TABLE_SCHEMA = DATABASE() AND REFERENCED_TABLE_NAME IS NOT NULL""", String.class);

			assertThat(keys).containsExactlyInAnyOrder(
					"users.role_id->roles.id",
					"role_permissions.role_id->roles.id",
					"role_permissions.permission_code->permissions.code",
					"equipment.category_id->categories.id",
					"loans.equipment_id->equipment.id",
					"loans.user_id->users.id",
					"password_tokens.user_id->users.id",
					"reminder_logs.run_id->reminder_runs.id",
					"reminder_logs.user_id->users.id",
					"reminder_log_items.log_id->reminder_logs.id",
					"reminder_log_items.equipment_id->equipment.id");
		}
	}

	@Nested
	@DisplayName("最初のデータ（データ設計 5.3・7章）")
	class InitialData {

		@Test
		@DisplayName("権限項目は 5.3 の18行で、コード・名前・区分・並び順が表のとおり")
		void permissions() {
			List<Map<String, Object>> rows = jdbc.queryForList(
					"SELECT code, name, group_name FROM permissions ORDER BY sort_order");

			assertThat(rows).extracting(r -> r.get("code") + "|" + r.get("name") + "|" + r.get("group_name"))
					.containsExactly(
							"EQUIPMENT_VIEW|備品一覧を見る|備品",
							"EQUIPMENT_CREATE|備品を登録する|備品",
							"EQUIPMENT_UPDATE|備品を編集する|備品",
							"EQUIPMENT_DELETE|備品を削除する|備品",
							"CATEGORY_MANAGE|種別を管理する|備品",
							"LOAN_SELF|備品を借りる・返す（自分）|貸出",
							"LOAN_MANAGE|貸出・返却を管理する|貸出",
							"LOAN_HISTORY_VIEW|貸出履歴を見る|貸出",
							"CONFIRM_SELF|最終確認日を更新する（自分）|確認",
							"CONFIRM_ALL|最終確認日を更新する（すべて）|確認",
							"CONFIRM_CSV|最終確認日をCSVで一括更新する|確認",
							"REMINDER_LOG_VIEW|催促履歴を見る|確認",
							"USER_VIEW|ユーザを見る|ユーザ・権限",
							"USER_CREATE|ユーザを登録する|ユーザ・権限",
							"USER_UPDATE|ユーザを編集する|ユーザ・権限",
							"USER_DELETE|ユーザを削除する|ユーザ・権限",
							"PERMISSION_VIEW|権限を見る|ユーザ・権限",
							"PERMISSION_MANAGE|権限を管理する|ユーザ・権限");
		}

		@Test
		@DisplayName("権限は「総務」（既定でない）と「一般社員」（既定）の2つ")
		void roles() {
			List<Map<String, Object>> rows = jdbc.queryForList(
					"SELECT name, is_default, deleted_at FROM roles ORDER BY id");

			assertThat(rows).extracting(r -> r.get("name") + "|" + r.get("is_default") + "|" + r.get("deleted_at"))
					.containsExactly("総務|false|null", "一般社員|true|null");
		}

		@Test
		@DisplayName("総務はすべての権限項目、一般社員は LOAN_SELF と CONFIRM_SELF だけを持つ（5.3 の表の○）")
		void rolePermissions() {
			Long soumuId = roleId("総務");
			Long ippanId = roleId("一般社員");

			assertThat(jdbc.queryForList(
					"SELECT permission_code FROM role_permissions WHERE role_id = ?", String.class, soumuId))
					.containsExactlyInAnyOrderElementsOf(
							jdbc.queryForList("SELECT code FROM permissions", String.class));
			assertThat(jdbc.queryForList(
					"SELECT permission_code FROM role_permissions WHERE role_id = ?", String.class, ippanId))
					.containsExactlyInAnyOrder("LOAN_SELF", "CONFIRM_SELF");
		}

		@Test
		@DisplayName("種別は A：PC、B：ヘッドセット、C：マウス、D：カメラ、E：モニター、F：アダプタ の6つ")
		void categories() {
			List<Map<String, Object>> rows = jdbc.queryForList(
					"SELECT initial, name FROM categories WHERE deleted_at IS NULL ORDER BY initial");

			assertThat(rows).extracting(r -> r.get("initial") + "：" + r.get("name")).containsExactly(
					"A：PC", "B：ヘッドセット", "C：マウス", "D：カメラ", "E：モニター", "F：アダプタ");
		}

		@Test
		@DisplayName("ユーザは最初の総務の担当者1人。権限は総務、パスワードは空、メールアドレスは設定の値")
		void initialAdmin() {
			Map<String, Object> row = jdbc.queryForMap(
					"SELECT name, email, password_hash, role_id, failed_login_count, version, created_by FROM users");

			assertThat(row.get("name")).isEqualTo("関谷 海仁");
			// application-test.yml の spring.flyway.placeholders.initial_admin_email
			assertThat(row.get("email")).isEqualTo("admin@example.com");
			assertThat(row.get("password_hash")).isNull();
			assertThat(row.get("role_id")).isEqualTo(roleId("総務"));
			assertThat(row.get("failed_login_count")).isEqualTo(0);
			assertThat(row.get("version")).isEqualTo(0);
			assertThat(row.get("created_by")).isNull();
		}

		@Test
		@DisplayName("最初のデータの作った日時は、日本時間の今（世界標準時から9時間ずれている）")
		void createdAtIsJapanTime() {
			Long diffHours = jdbc.queryForObject(
					"SELECT TIMESTAMPDIFF(HOUR, UTC_TIMESTAMP(), created_at) FROM roles WHERE name = '総務'", Long.class);

			// テストの前に当て直したばかりなので、作った日時は今とほぼ同じ（+9時間）
			assertThat(diffHours).isBetween(8L, 9L);
		}
	}

	@Nested
	@DisplayName("削除済みを重複に数えない（データ設計 2章）")
	class UniqueExceptDeleted {

		@Test
		@DisplayName("削除されていない種別と同じ名前は登録できない")
		void duplicateCategoryName() {
			assertThatThrownBy(() -> insertCategory("PC", null))
					.isInstanceOf(DuplicateKeyException.class)
					.hasMessageContaining("uk_categories_name_active");
		}

		@Test
		@DisplayName("削除されていない種別と同じ頭文字は登録できない")
		void duplicateCategoryInitial() {
			assertThatThrownBy(() -> insertCategory("タブレット", "A"))
					.isInstanceOf(DuplicateKeyException.class)
					.hasMessageContaining("uk_categories_initial_active");
		}

		@Test
		@DisplayName("頭文字が空の種別は、いくつでも登録できる")
		void manyCategoriesWithoutInitial() {
			assertThatCode(() -> {
				insertCategory("タブレット", null);
				insertCategory("キーボード", null);
			}).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("削除済みの種別と同じ名前・頭文字は登録できる")
		void sameNameAsDeletedCategory() {
			jdbc.update("UPDATE categories SET deleted_at = " + NOW + " WHERE name = 'PC'");

			assertThatCode(() -> insertCategory("PC", "A")).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("大文字と小文字、ひらがなとカタカナ、「ハ」「バ」「パ」は別の名前として扱う")
		void collationDistinguishesCharacters() {
			assertThatCode(() -> {
				insertCategory("pc", null);
				insertCategory("まうす", null);
				insertCategory("ハック", null);
				insertCategory("バック", null);
				insertCategory("パック", null);
			}).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("削除されていない備品と同じ管理番号は登録できず、削除済みとなら登録できる")
		void managementNo() {
			insertEquipment("A001", "MANAGED", null);

			assertThatThrownBy(() -> insertEquipment("A001", "MANAGED", null))
					.isInstanceOf(DuplicateKeyException.class)
					.hasMessageContaining("uk_equipment_management_no_active");

			jdbc.update("UPDATE equipment SET deleted_at = " + NOW + " WHERE management_no = 'A001'");
			assertThatCode(() -> insertEquipment("A001", "MANAGED", null)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("削除されていないユーザと同じメールアドレスは登録できない")
		void duplicateEmail() {
			assertThatThrownBy(() -> insertUser("admin@example.com"))
					.isInstanceOf(DuplicateKeyException.class)
					.hasMessageContaining("uk_users_email_active");
		}

		@Test
		@DisplayName("削除済みのユーザと同じメールアドレスは登録できる")
		void sameEmailAsDeletedUser() {
			insertUser("yamada@example.com");
			jdbc.update("UPDATE users SET deleted_at = " + NOW + " WHERE email = 'yamada@example.com'");

			assertThatCode(() -> insertUser("yamada@example.com")).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("削除済みの権限と同じ名前は登録できる")
		void sameNameAsDeletedRole() {
			String sql = "INSERT INTO roles (name, is_default, created_at, updated_at) VALUES ('閲覧者', FALSE, " + NOW
					+ ", " + NOW + ")";
			jdbc.update(sql);
			jdbc.update("UPDATE roles SET deleted_at = " + NOW + " WHERE name = '閲覧者'");

			assertThatCode(() -> jdbc.update(sql)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("削除されていない権限と同じ名前は登録できない")
		void duplicateRoleName() {
			assertThatThrownBy(() -> jdbc.update(
					"INSERT INTO roles (name, is_default, created_at, updated_at) VALUES ('総務', FALSE, " + NOW + ", "
							+ NOW + ")"))
					.isInstanceOf(DuplicateKeyException.class)
					.hasMessageContaining("uk_roles_name_active");
		}
	}

	@Nested
	@DisplayName("二重貸出の防止（データ設計 5.7）")
	class DoubleLoan {

		@Test
		@DisplayName("貸出中の備品に、もう1つ貸出中の行は作れない")
		void cannotLendTwice() {
			long equipmentId = insertEquipment("A001", "MANAGED", null);
			long userId = adminId();
			insertLoan(equipmentId, userId, "2026-11-01", null, null);

			assertThatThrownBy(() -> insertLoan(equipmentId, userId, "2026-11-02", null, null))
					.isInstanceOf(DuplicateKeyException.class)
					.hasMessageContaining("uk_loans_active_equipment_id");
		}

		@Test
		@DisplayName("返却した備品は、また貸し出せる")
		void canLendAfterReturn() {
			long equipmentId = insertEquipment("A001", "MANAGED", null);
			long userId = adminId();
			insertLoan(equipmentId, userId, "2026-11-01", null, "2026-11-05");

			assertThatCode(() -> insertLoan(equipmentId, userId, "2026-11-06", null, null))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("取り消した貸出（deleted_at が入った行）は、貸出中に数えない")
		void canLendAfterCancel() {
			long equipmentId = insertEquipment("A001", "MANAGED", null);
			long userId = adminId();
			long loanId = insertLoan(equipmentId, userId, "2026-11-01", null, null);
			jdbc.update("UPDATE loans SET deleted_at = " + NOW + " WHERE id = ?", loanId);

			assertThatCode(() -> insertLoan(equipmentId, userId, "2026-11-01", null, null))
					.doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("入ってよい値の決まり（CHECK 制約。データ設計 5.6・5.7）")
	class Checks {

		@Test
		@DisplayName("管理状態は MANAGED・DISPOSED・MISSING だけ入る")
		void status() {
			assertThatCode(() -> {
				insertEquipment("A001", "MANAGED", null);
				insertEquipment("A002", "MISSING", null);
				insertEquipment("A003", "DISPOSED", "2026-11-01");
			}).doesNotThrowAnyException();

			assertThatThrownBy(() -> insertEquipment("A004", "LOST", null))
					.isInstanceOf(DataAccessException.class)
					.hasMessageContaining("ck_equipment_status");
		}

		@Test
		@DisplayName("廃棄日は、管理状態が DISPOSED のときだけ入る")
		void disposedOn() {
			assertThatThrownBy(() -> insertEquipment("A001", "MANAGED", "2026-11-01"))
					.isInstanceOf(DataAccessException.class)
					.hasMessageContaining("ck_equipment_disposed_on");
		}

		@Test
		@DisplayName("返却予定日・返却日は、貸出日と同じ日なら入り、前の日は入らない")
		void loanDates() {
			long equipmentId = insertEquipment("A001", "MANAGED", null);
			long userId = adminId();

			assertThatCode(() -> insertLoan(equipmentId, userId, "2026-11-10", "2026-11-10", "2026-11-10"))
					.doesNotThrowAnyException();
			assertThatThrownBy(() -> insertLoan(equipmentId, userId, "2026-11-10", "2026-11-09", null))
					.isInstanceOf(DataAccessException.class)
					.hasMessageContaining("ck_loans_due_on");
			assertThatThrownBy(() -> insertLoan(equipmentId, userId, "2026-11-10", null, "2026-11-09"))
					.isInstanceOf(DataAccessException.class)
					.hasMessageContaining("ck_loans_returned_on");
		}
	}

	@Nested
	@DisplayName("そのほかの決まり")
	class Others {

		@Test
		@DisplayName("ない種別を指す備品は登録できない（外部キー）")
		void foreignKey() {
			assertThatThrownBy(() -> jdbc.update("""
					INSERT INTO equipment (management_no, category_id, status, created_at, updated_at)
					VALUES ('A001', 9999, 'MANAGED', %s, %s)""".formatted(NOW, NOW)))
					.isInstanceOf(DataAccessException.class)
					.hasMessageContaining("fk_equipment_category_id");
		}

		@Test
		@DisplayName("同じ月の催促の実行は1回しか記録できない")
		void reminderRunOncePerMonth() {
			String sql = "INSERT INTO reminder_runs (target_month, started_at, success_count, failure_count)"
					+ " VALUES ('2026-11', " + NOW + ", 0, 0)";
			jdbc.update(sql);

			assertThatThrownBy(() -> jdbc.update(sql))
					.isInstanceOf(DuplicateKeyException.class)
					.hasMessageContaining("uk_reminder_runs_target_month");
		}

		@Test
		@DisplayName("同じ合言葉のハッシュは2つ保存できない")
		void tokenHashUnique() {
			String sql = "INSERT INTO password_tokens (user_id, token_hash, purpose, expires_at, created_at)"
					+ " VALUES (?, REPEAT('a', 64), 'RESET', " + NOW + ", " + NOW + ")";
			jdbc.update(sql, adminId());

			assertThatThrownBy(() -> jdbc.update(sql, adminId()))
					.isInstanceOf(DuplicateKeyException.class)
					.hasMessageContaining("uk_password_tokens_token_hash");
		}
	}

	private Long roleId(String name) {
		return jdbc.queryForObject("SELECT id FROM roles WHERE name = ?", Long.class, name);
	}

	private long adminId() {
		return jdbc.queryForObject("SELECT id FROM users WHERE email = 'admin@example.com'", Long.class);
	}

	private void insertCategory(String name, String initial) {
		jdbc.update("INSERT INTO categories (name, initial, created_at, updated_at) VALUES (?, ?, " + NOW + ", " + NOW
				+ ")", name, initial);
	}

	private void insertUser(String email) {
		jdbc.update("INSERT INTO users (name, email, role_id, created_at, updated_at) VALUES ('山田 太郎', ?, ?, " + NOW
				+ ", " + NOW + ")", email, roleId("一般社員"));
	}

	private long insertEquipment(String managementNo, String status, String disposedOn) {
		jdbc.update("""
				INSERT INTO equipment (management_no, category_id, status, disposed_on, created_at, updated_at)
				VALUES (?, ?, ?, ?, %s, %s)""".formatted(NOW, NOW),
				managementNo, pcCategoryId(), status, disposedOn);
		return lastInsertId();
	}

	private long insertLoan(long equipmentId, long userId, String lentOn, String dueOn, String returnedOn) {
		jdbc.update("""
				INSERT INTO loans (equipment_id, user_id, lent_on, due_on, returned_on, created_at, updated_at)
				VALUES (?, ?, ?, ?, ?, %s, %s)""".formatted(NOW, NOW),
				equipmentId, userId, lentOn, dueOn, returnedOn);
		return lastInsertId();
	}

	/** 最初のデータの種別「PC」の id。 */
	private long pcCategoryId() {
		return jdbc.queryForObject("SELECT id FROM categories WHERE name = 'PC' AND deleted_at IS NULL", Long.class);
	}

	private long lastInsertId() {
		return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}
}
