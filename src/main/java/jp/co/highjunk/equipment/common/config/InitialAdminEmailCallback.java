package jp.co.highjunk.equipment.common.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.springframework.stereotype.Component;

/**
 * 最初のデータ（V2）を当てる前に、最初の総務の担当者のメールアドレスが設定されているかを確かめる（詳細設計 テーブル.md 5章）。
 * <p>
 * 設定がないまま V2 を当てると、V2 は失敗して書き込みは元に戻るが、Flyway の記録に「V2 は失敗した」が残り、
 * 設定したあとも、その記録を人が消すまで起動できなくなる。そのため、Flyway が当て始める前（BEFORE_MIGRATE）に止める。
 * 設定の値は、SQL の '…' の中にそのまま入る（Flyway のプレースホルダー）。そのため、空のときに加えて、
 * SQL を壊す文字（' " \）や、SQL の TRIM で除けない空白（タブ・改行など）を含むとき、@ がないときも止める。
 * 止める文には、設定の値を入れない（秘密のため。docs/API設計.md 6章）。
 * V2 がすでに当たっているデータベースでは、設定がなくても止めない（最初のデータを入れ終えたあとは使わないため）。
 * Spring Boot は、この部品（Flyway の Callback）を自動で Flyway に渡す。
 */
@Component
public class InitialAdminEmailCallback implements Callback {

	/** V2 の SQL の中の置き換える印の名前（application.yml の spring.flyway.placeholders）。 */
	static final String PLACEHOLDER = "initial_admin_email";

	/** 最初のデータを入れる Flyway の版。 */
	static final String INITIAL_DATA_VERSION = "2";

	static final String MESSAGE = "最初の総務の担当者のメールアドレス（app.initial-admin-email）が設定されていません。"
			+ "開発では secret.yml、AWS では環境変数 APP_INITIAL_ADMIN_EMAIL に設定してから起動してください（docs/データ設計.md 7章）。";

	static final String INVALID_MESSAGE = "最初の総務の担当者のメールアドレス（app.initial-admin-email）の形が正しくありません。"
			+ "@ を1つ含み、空白・タブ・改行・「'」「\"」「\\」を含まない値にしてから起動してください（前後の半角スペースは除かれます）。";

	/**
	 * 受け付けるメールアドレスの形。前後の半角スペースだけを許し（V2 の SQL の TRIM で除ける）、
	 * 中には空白・制御文字（タブ・改行など）・SQL を壊す文字（' " \）を含まず、@ を1つだけ含む。
	 */
	private static final Pattern EMAIL = Pattern
			.compile("^ *[^\\s'\"\\\\@\\p{Cntrl}]+@[^\\s'\"\\\\@\\p{Cntrl}]+ *$");

	/** Flyway の記録のテーブルの名前に使ってよい文字（SQL に名前を入れるため、決まった文字だけにする）。 */
	private static final Pattern TABLE_NAME = Pattern.compile("[A-Za-z0-9_]+");

	@Override
	public boolean supports(Event event, Context context) {
		return event == Event.BEFORE_MIGRATE;
	}

	@Override
	public boolean canHandleInTransaction(Event event, Context context) {
		return true;
	}

	@Override
	public void handle(Event event, Context context) {
		String email = context.getConfiguration().getPlaceholders().get(PLACEHOLDER);
		boolean blank = email == null || email.isBlank();
		if (!blank && EMAIL.matcher(email).matches()) {
			return;
		}
		if (initialDataApplied(context.getConnection(), context.getConfiguration().getTable())) {
			return;
		}
		throw new FlywayException(blank ? MESSAGE : INVALID_MESSAGE);
	}

	@Override
	public String getCallbackName() {
		return "initialAdminEmailCheck";
	}

	/**
	 * Flyway の記録で、最初のデータ（V2）がすでに当たっているかを調べる。
	 * 記録のテーブルがまだない（初めて当てる）ときは、当たっていない。
	 */
	private boolean initialDataApplied(Connection connection, String table) {
		if (!TABLE_NAME.matcher(table).matches()) {
			throw new FlywayException("Flyway の記録のテーブルの名前が想定と違います：" + table);
		}
		try {
			try (ResultSet tables = connection.getMetaData()
					.getTables(connection.getCatalog(), null, table, new String[] { "TABLE" })) {
				if (!tables.next()) {
					return false;
				}
			}
			// テーブルの名前は値として渡せないので、上で文字を確かめたものを入れる。値は ? で渡す
			String sql = "SELECT COUNT(*) FROM `" + table + "` WHERE version = ? AND success = TRUE";
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				statement.setString(1, INITIAL_DATA_VERSION);
				try (ResultSet result = statement.executeQuery()) {
					return result.next() && result.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			throw new FlywayException("Flyway の記録を読めませんでした", e);
		}
	}
}
