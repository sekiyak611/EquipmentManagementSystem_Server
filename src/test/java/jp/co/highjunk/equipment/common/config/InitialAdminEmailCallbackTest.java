package jp.co.highjunk.equipment.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 最初の総務の担当者のメールアドレスの確かめ（詳細設計 テーブル.md 5章）のテスト。
 * Flyway の記録は、テスト専用のデータベース（equipment_test）の本物を読む（V2 は当たっている）。
 */
@SpringBootTest
class InitialAdminEmailCallbackTest {

	@Autowired
	DataSource dataSource;

	@Autowired
	InitialAdminEmailCallback callback;

	@Test
	@DisplayName("Flyway が当て始める前（BEFORE_MIGRATE）だけに動く")
	void supportsOnlyBeforeMigrate() {
		Context context = mock(Context.class);

		assertThat(callback.supports(Event.BEFORE_MIGRATE, context)).isTrue();
		assertThat(callback.supports(Event.BEFORE_EACH_MIGRATE, context)).isFalse();
		assertThat(callback.supports(Event.BEFORE_CLEAN, context)).isFalse();
	}

	@Test
	@DisplayName("メールアドレスが設定されていれば、データベースを見ずに通す")
	void emailSet() {
		Context context = context("admin@example.com", "flyway_schema_history", null);

		assertThatCode(() -> callback.handle(Event.BEFORE_MIGRATE, context)).doesNotThrowAnyException();
		verify(context, never()).getConnection();
	}

	@Test
	@DisplayName("メールアドレスが空でも、最初のデータ（V2）がすでに当たっていれば通す")
	void emptyButInitialDataApplied() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			Context context = context("", "flyway_schema_history", connection);

			assertThatCode(() -> callback.handle(Event.BEFORE_MIGRATE, context)).doesNotThrowAnyException();
		}
	}

	@Test
	@DisplayName("メールアドレスが空で、初めて当てる（Flyway の記録のテーブルがない）ときは、設定の場所を書いた文で止める")
	void emptyOnNewDatabase() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			// 記録のテーブルがないデータベースの代わりに、ない名前のテーブルを記録のテーブルとして渡す
			Context context = context(" ", "no_such_history", connection);

			assertThatThrownBy(() -> callback.handle(Event.BEFORE_MIGRATE, context))
					.isInstanceOf(FlywayException.class)
					.hasMessage(InitialAdminEmailCallback.MESSAGE);
		}
	}

	@Test
	@DisplayName("メールアドレスの設定そのものがない（null）ときも止める")
	void missingOnNewDatabase() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			Context context = context(null, "no_such_history", connection);

			assertThatThrownBy(() -> callback.handle(Event.BEFORE_MIGRATE, context))
					.isInstanceOf(FlywayException.class)
					.hasMessage(InitialAdminEmailCallback.MESSAGE);
		}
	}

	@Test
	@DisplayName("記録のテーブルの名前に決まった文字以外があれば、SQL に入れずに止める")
	void rejectsUnexpectedTableName() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			Context context = context("", "x`; DROP TABLE users; --", connection);

			assertThatThrownBy(() -> callback.handle(Event.BEFORE_MIGRATE, context))
					.isInstanceOf(FlywayException.class)
					.hasMessageContaining("想定と違います");
		}
	}

	@ParameterizedTest(name = "[{index}] 「{0}」")
	@ValueSource(strings = { " admin@example.com ", "Admin@Example.COM", "a.b+c@example.co.jp" })
	@DisplayName("前後に半角スペースがある・大文字を含むメールアドレスは通す（V2 の SQL で前後の空白を除き、小文字にそろえる）")
	void acceptsValidForms(String email) {
		Context context = context(email, "no_such_history", null);

		assertThatCode(() -> callback.handle(Event.BEFORE_MIGRATE, context)).doesNotThrowAnyException();
		verify(context, never()).getConnection();
	}

	@ParameterizedTest(name = "[{index}] 「{0}」")
	@ValueSource(strings = { "o'brien@example.com", "a\"b@example.com", "a\\b@example.com", "admin@example.com\t",
			"admin@example.com\n", "admin example@example.com", "admin.example.com", "a@b@example.com", "@example.com",
			"admin@" })
	@DisplayName("初めて当てるとき、SQL を壊す文字・TRIM で除けない空白・@ の数の誤りがあれば、値を出さない文で止める")
	void rejectsInvalidForms(String email) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			Context context = context(email, "no_such_history", connection);

			assertThatThrownBy(() -> callback.handle(Event.BEFORE_MIGRATE, context))
					.isInstanceOf(FlywayException.class)
					.hasMessage(InitialAdminEmailCallback.INVALID_MESSAGE);
		}
	}

	@Test
	@DisplayName("形の正しくない値でも、最初のデータ（V2）がすでに当たっていれば通す")
	void invalidButInitialDataApplied() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			Context context = context("o'brien@example.com", "flyway_schema_history", connection);

			assertThatCode(() -> callback.handle(Event.BEFORE_MIGRATE, context)).doesNotThrowAnyException();
		}
	}

	@Test
	@DisplayName("記録のテーブルはあるが V2 の成功がない（V1 だけ当たった・V2 が失敗として残っている）ときは止める")
	void historyWithoutSuccessfulV2() throws Exception {
		// 記録のテーブルと同じ形のテーブルを別の名前で作る（テーブルを作る命令は元に戻せないので、最後に消す）
		try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE test_history_without_v2 (version VARCHAR(50), success BOOLEAN NOT NULL)");
			try {
				statement.execute("INSERT INTO test_history_without_v2 VALUES ('1', TRUE), ('2', FALSE)");
				Context context = context("", "test_history_without_v2", connection);

				assertThatThrownBy(() -> callback.handle(Event.BEFORE_MIGRATE, context))
						.isInstanceOf(FlywayException.class)
						.hasMessage(InitialAdminEmailCallback.MESSAGE);
			} finally {
				statement.execute("DROP TABLE test_history_without_v2");
			}
		}
	}

	private Context context(String email, String table, Connection connection) {
		Map<String, String> placeholders = new HashMap<>();
		if (email != null) {
			placeholders.put(InitialAdminEmailCallback.PLACEHOLDER, email);
		}
		Configuration configuration = mock(Configuration.class);
		when(configuration.getPlaceholders()).thenReturn(placeholders);
		when(configuration.getTable()).thenReturn(table);
		Context context = mock(Context.class);
		when(context.getConfiguration()).thenReturn(configuration);
		when(context.getConnection()).thenReturn(connection);
		return context;
	}
}
