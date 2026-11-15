package jp.co.highjunk.equipment.common.slack;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.common.error.ApiResponse;
import lombok.RequiredArgsConstructor;

/**
 * テスト用：偽物（FakeSlackSender）が記録した、送ったDMを読み出す・空にする（詳細設計 common.md 3.13）。
 * app.test-support.enabled が true のときだけ動く。本番・開発ではこのクラスがなく、404 になる。
 * ログインなし・CSRFの合言葉なしで呼べる（SecurityConfig）。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test-support/slack-messages")
@ConditionalOnProperty(prefix = "app.test-support", name = "enabled", havingValue = "true")
public class SlackTestSupportController {

	private final FakeSlackSender fakeSlackSender;

	/** 送った内容の一覧を、古い順に返す。 */
	@GetMapping
	public ApiResponse<SentSlackMessagesResponse> findAll() {
		return ApiResponse.ok(new SentSlackMessagesResponse(fakeSlackSender.findAll()));
	}

	/** 記録を空にする。 */
	@DeleteMapping
	public ApiResponse<Void> clear() {
		fakeSlackSender.clear();
		return ApiResponse.ok(null);
	}
}
