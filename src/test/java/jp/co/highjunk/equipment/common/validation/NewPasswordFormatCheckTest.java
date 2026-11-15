package jp.co.highjunk.equipment.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 新しいパスワードの形式のチェック（画面設計 8章。詳細設計 common.md 3.15）の単体テスト。
 */
class NewPasswordFormatCheckTest {

	private static String messageFor(String value) {
		return NewPasswordFormatCheck.messageFor(value);
	}

	@Test
	@DisplayName("空（null・長さ0）：「新しいパスワードを入力してください」")
	void empty() {
		assertThat(messageFor(null)).isEqualTo("新しいパスワードを入力してください");
		assertThat(messageFor("")).isEqualTo("新しいパスワードを入力してください");
	}

	@Test
	@DisplayName("7文字：短い。8文字：使える（境界値）")
	void minLength() {
		assertThat(messageFor("a".repeat(7))).isEqualTo("パスワードは8文字以上で入力してください");
		assertThat(messageFor("a".repeat(8))).isNull();
	}

	@Test
	@DisplayName("64文字：使える。65文字：長い（境界値）")
	void maxLength() {
		assertThat(messageFor("a".repeat(64))).isNull();
		assertThat(messageFor("a".repeat(65))).isEqualTo("パスワードは64文字以内で入力してください");
	}

	@Test
	@DisplayName("使える文字：半角の英字・数字・記号・スペース（0x20〜0x7E）。スペースだけ・前後のスペースも使える（取らない）")
	void allowedCharacters() {
		assertThat(messageFor(" Ab1!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~ ")).isNull();
		assertThat(messageFor("        ")).isNull();
	}

	@Test
	@DisplayName("使えない文字：日本語・全角の英数字・タブ・改行・絵文字・DEL（0x7F）は「使えるのは、…です」")
	void badCharacters() {
		String message = "パスワードに使えるのは、半角の英字・数字・記号・スペースです";
		assertThat(messageFor("パスワードパスワード")).isEqualTo(message);
		assertThat(messageFor("ＡＢＣＤＥＦＧＨ")).isEqualTo(message);
		assertThat(messageFor("abcdefg\t")).isEqualTo(message);
		assertThat(messageFor("abcdefg\n")).isEqualTo(message);
		assertThat(messageFor("abcdefg\u007f")).isEqualTo(message);
		assertThat(messageFor("abcdefg😀")).isEqualTo(message);
	}

	@Test
	@DisplayName("1つの欄に1つだけ：使えない文字は、短い・長いより先に出る")
	void orderOfMessages() {
		assertThat(messageFor("あ")).isEqualTo("パスワードに使えるのは、半角の英字・数字・記号・スペースです");
		assertThat(messageFor("あ".repeat(70))).isEqualTo("パスワードに使えるのは、半角の英字・数字・記号・スペースです");
	}
}
