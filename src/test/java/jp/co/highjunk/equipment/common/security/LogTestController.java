package jp.co.highjunk.equipment.common.security;

import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.common.error.ApiResponse;

/**
 * ログに付くユーザIDを確かめるための、テストの中だけにあるAPI（本番には入らない）。
 */
@RestController
class LogTestController {

	@GetMapping("/api/test-security/log-user-id")
	ApiResponse<String> logUserId() {
		return ApiResponse.ok(MDC.get(UserIdLogFilter.MDC_KEY));
	}
}
