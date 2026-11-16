package jp.co.highjunk.equipment.common.passwordlink;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import lombok.RequiredArgsConstructor;

/**
 * テスト用：パスワード設定用のリンクを作る・全部消す（詳細設計 common.md 3.14）。
 * app.test-support.enabled が true のときだけ動く。本番・開発ではこのクラスがなく、404 になる。
 * ログインなし・CSRFの合言葉なしで呼べる（SecurityConfig）。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test-support/password-links")
@ConditionalOnProperty(prefix = "app.test-support", name = "enabled", havingValue = "true")
public class PasswordLinkTestSupportController {

	private final PasswordLinkTestSupportService service;

	/** 指定したユーザに、指定した用途のリンクを作り、合言葉を返す（前のリンクは無効になる）。 */
	@PostMapping
	public ApiResponse<PasswordLinkTestSupportIssued> issue(@Valid @RequestBody PasswordLinkTestSupportRequest request) {
		return ApiResponse.ok(service.issue(request.getEmail(), request.getPurpose()));
	}

	/** リンクを全部消す。 */
	@DeleteMapping
	public ApiResponse<Void> clear() {
		service.clear();
		return ApiResponse.ok(null);
	}
}
