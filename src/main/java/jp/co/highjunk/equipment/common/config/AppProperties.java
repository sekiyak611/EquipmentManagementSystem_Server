package jp.co.highjunk.equipment.common.config;

import java.time.Duration;
import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * このシステムの設定値（docs/API設計.md 6章）。
 * application.yml の「app:」の下の値を読み込む。プログラムに直接書かず、ここから読む（CLAUDE.md 5章）。
 *
 * @param baseUrl            DMに入れるリンクのもとになるアドレス
 * @param timeZone           時刻の基準（日本時間）
 * @param reminderCron       催促の処理を動かす時刻
 * @param rememberMeValidity ログインしたままにする期間
 * @param passwordLink       パスワード設定用のリンクの設定
 * @param lockThreshold      ログインに何回続けて失敗したらロックするか
 * @param csvMaxSize         アップロードするCSVの大きさの上限
 * @param slack              Slackの設定
 * @param initialAdminEmail  最初の総務の担当者のメールアドレス（秘密）
 * @param testSupport        テスト用の切り替え
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
		@NotBlank String baseUrl,
		@NotNull ZoneId timeZone,
		@NotBlank String reminderCron,
		@NotNull Duration rememberMeValidity,
		@NotNull @Valid PasswordLink passwordLink,
		@Min(1) int lockThreshold,
		@NotNull DataSize csvMaxSize,
		@NotNull @Valid Slack slack,
		String initialAdminEmail,
		@NotNull @Valid TestSupport testSupport) {

	/**
	 * パスワード設定用のリンクの設定。
	 *
	 * @param resetValidity        再設定のリンクの有効期限
	 * @param lockValidity         ロックしたときに送るリンクの有効期限
	 * @param registrationValidity ユーザ登録のときに送るリンクの有効期限
	 * @param requestInterval      同じメールアドレスへの再設定の依頼の間隔
	 */
	public record PasswordLink(
			@NotNull Duration resetValidity,
			@NotNull Duration lockValidity,
			@NotNull Duration registrationValidity,
			@NotNull Duration requestInterval) {
	}

	/**
	 * Slackの設定。
	 *
	 * @param botToken ボットトークン（秘密。ログに出さない）
	 */
	public record Slack(String botToken) {

		/** 記録に出たときにトークンが見えないようにする。 */
		@Override
		public String toString() {
			return "Slack[botToken=" + (botToken == null || botToken.isBlank() ? "(なし)" : "(秘密)") + "]";
		}
	}

	/**
	 * テスト用の切り替え。本番では必ず false。
	 *
	 * @param enabled true のとき、Slackの偽物と、送った内容を読み出すAPIが動く
	 */
	public record TestSupport(boolean enabled) {
	}

	/** 記録に出たときに、秘密の値（メールアドレス）が見えないようにする。 */
	@Override
	public String toString() {
		return "AppProperties[baseUrl=" + baseUrl + ", timeZone=" + timeZone + ", reminderCron=" + reminderCron
				+ ", rememberMeValidity=" + rememberMeValidity + ", passwordLink=" + passwordLink
				+ ", lockThreshold=" + lockThreshold + ", csvMaxSize=" + csvMaxSize + ", slack=" + slack
				+ ", initialAdminEmail=(秘密), testSupport=" + testSupport + "]";
	}
}
