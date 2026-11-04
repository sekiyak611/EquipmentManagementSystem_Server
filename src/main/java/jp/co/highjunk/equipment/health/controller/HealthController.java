package jp.co.highjunk.equipment.health.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.common.error.ApiResponse;
import jp.co.highjunk.equipment.dto.health.HealthResponse;
import jp.co.highjunk.equipment.health.service.HealthService;

/**
 * health 機能のAPIの入口（A-08）。
 * 見張り役（AWSのLambda）が5分ごとに呼ぶ。ログインしていなくても呼べる（common.security.SecurityConfig）。
 */
@RestController
public class HealthController {

	private final HealthService healthService;

	public HealthController(HealthService healthService) {
		this.healthService = healthService;
	}

	/**
	 * A-08 アプリとデータベースが動いているかを答える。
	 * 動いていれば 200（data は { "state": "UP" }）。データベースにつながらなければ 503（HealthService が投げる）。
	 */
	@GetMapping("/api/health")
	public ApiResponse<HealthResponse> health() {
		return ApiResponse.ok(healthService.check());
	}
}
