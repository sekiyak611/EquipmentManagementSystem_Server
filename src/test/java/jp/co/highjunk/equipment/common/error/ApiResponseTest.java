package jp.co.highjunk.equipment.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 返す値の形（docs/API設計.md 2.4）の単体テスト。
 */
class ApiResponseTest {

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	@Test
	@DisplayName("正常時：status・message・data・errors の4つがあり、errors は null")
	void ok() {
		JsonNode json = jsonMapper.valueToTree(ApiResponse.ok(Map.of("userId", 1)));
		assertThat(json.get("status").asInt()).isEqualTo(200);
		assertThat(json.get("status").isNumber()).isTrue();
		assertThat(json.get("message").asString()).isEqualTo("処理が正常に完了しました。");
		assertThat(json.get("data").get("userId").asInt()).isEqualTo(1);
		assertThat(json.has("errors")).isTrue();
		assertThat(json.get("errors").isNull()).isTrue();
	}

	@Test
	@DisplayName("作った・受け付けた：status は 201・202、返すものがないときは data が null")
	void createdAndAccepted() {
		assertThat(ApiResponse.created("x").status()).isEqualTo(201);
		JsonNode accepted = jsonMapper.valueToTree(ApiResponse.accepted());
		assertThat(accepted.get("status").asInt()).isEqualTo(202);
		assertThat(accepted.get("message").asString()).isEqualTo("処理が正常に完了しました。");
		assertThat(accepted.get("data").isNull()).isTrue();
	}

	@Test
	@DisplayName("入力の誤り：data は null、errors に入力欄ごとのメッセージ。行番号・管理番号の項目は出さない")
	void validationError() {
		JsonNode json = jsonMapper.valueToTree(ApiResponse.error(HttpStatus.BAD_REQUEST,
				ApiMessages.VALIDATION_ERROR, List.of(ErrorItem.ofField("userName", "ユーザー名は必須です。"))));
		assertThat(json.get("status").asInt()).isEqualTo(400);
		assertThat(json.get("message").asString()).isEqualTo("入力内容に誤りがあります。");
		assertThat(json.get("data").isNull()).isTrue();
		JsonNode item = json.get("errors").get(0);
		assertThat(item.get("field").asString()).isEqualTo("userName");
		assertThat(item.get("message").asString()).isEqualTo("ユーザー名は必須です。");
		assertThat(item.has("row")).isFalse();
		assertThat(item.has("managementNo")).isFalse();
	}

	@Test
	@DisplayName("CSVの行のエラー：errors の1件に row と managementNo が入り、field は null")
	void csvRowError() {
		JsonNode item = jsonMapper.valueToTree(ErrorItem.ofCsvRow(12, "D003", "不明の備品です。"));
		assertThat(item.has("field")).isTrue();
		assertThat(item.get("field").isNull()).isTrue();
		assertThat(item.get("row").asInt()).isEqualTo(12);
		assertThat(item.get("managementNo").asString()).isEqualTo("D003");
		assertThat(item.get("message").asString()).isEqualTo("不明の備品です。");
	}
}
