package jp.co.highjunk.equipment;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * SQL（MyBatis の XML）の書き方の決まりを、機械で確かめるテスト（CLAUDE.md 5章。Server の .claude/rules/java.md）。
 * データベースは使わない。XML のファイルを読んで、決まりに合わない書き方がないかを調べる。
 * 調べるのは、src/main/resources/jp/co/highjunk/equipment/dao/mapper/ の下のすべての XML（MyBatis Generator が作ったものと、手で書いたもの）。
 */
@DisplayName("SQL（Mapper の XML）の書き方")
class MapperXmlRulesTest {

	private static final Path MAPPER_DIR = Path.of("src/main/resources/jp/co/highjunk/equipment/dao/mapper");

	private static final Path CUSTOM_DIR = MAPPER_DIR.resolve("custom");

	/** JOIN（INNER JOIN・LEFT JOIN など）。SQL の文の中の単語として */
	private static final Pattern JOIN = Pattern.compile("\\bjoin\\b", Pattern.CASE_INSENSITIVE);

	/** サブクエリ：かっこの中の SELECT */
	private static final Pattern SUBQUERY = Pattern.compile("\\(\\s*select\\b", Pattern.CASE_INSENSITIVE);

	/** 値をつなげて SQL を作る書き方（SQL インジェクションの元）。#{} は使ってよい */
	private static final Pattern DOLLAR = Pattern.compile("\\$\\{");

	private static List<Path> xmlFiles(Path dir) throws IOException {
		try (Stream<Path> files = Files.walk(dir)) {
			return files.filter(p -> p.toString().endsWith(".xml")).toList();
		}
	}

	/** XML のコメント（&lt;!-- --&gt;）を除いた中身。説明の文に「JOIN を使わない」と書いてあっても、違反にしないため */
	private static String withoutComments(Path file) throws IOException {
		return Files.readString(file, StandardCharsets.UTF_8).replaceAll("(?s)<!--.*?-->", "");
	}

	private static List<String> violations(List<Path> files, Pattern pattern) throws IOException {
		List<String> found = new ArrayList<>();
		for (Path file : files) {
			if (pattern.matcher(withoutComments(file)).find()) {
				found.add(file.toString());
			}
		}
		return found;
	}

	@Test
	@DisplayName("調べる XML が見つかる（フォルダを間違えて、何も調べないまま成功にしない）")
	void filesExist() throws IOException {
		assertThat(xmlFiles(MAPPER_DIR)).isNotEmpty();
		assertThat(xmlFiles(CUSTOM_DIR)).isNotEmpty();
	}

	@Test
	@DisplayName("JOIN を使わない（複数のテーブルは、テーブルごとに読んで、Java で突き合わせる）")
	void noJoin() throws IOException {
		assertThat(violations(xmlFiles(MAPPER_DIR), JOIN)).isEmpty();
	}

	@Test
	@DisplayName("サブクエリ（かっこの中の SELECT）を使わない")
	void noSubquery() throws IOException {
		assertThat(violations(xmlFiles(MAPPER_DIR), SUBQUERY)).isEmpty();
	}

	@Test
	@DisplayName("手で書いた SQL（custom）で、値を ${} でつなげない（#{} で渡す）。MyBatis Generator が作った Example の並び順（orderByClause）は対象外")
	void noDollarInCustom() throws IOException {
		assertThat(violations(xmlFiles(CUSTOM_DIR), DOLLAR)).isEmpty();
	}
}
