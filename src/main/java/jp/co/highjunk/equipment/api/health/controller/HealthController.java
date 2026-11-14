package jp.co.highjunk.equipment.api.health.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.highjunk.equipment.api.health.model.HealthRestOutDto;
import jp.co.highjunk.equipment.api.health.service.HealthService;
import jp.co.highjunk.equipment.common.error.ApiResponse;
import lombok.RequiredArgsConstructor;

/**
 * A-08 アプリとデータベースが動いているかを答える（詳細設計 health/A-08_動いているかを答える.md）。
 * 見張り役（AWSのLambda）が5分ごとに呼ぶ。ログインしていなくても呼べる（common.security.SecurityConfig）。
 */
@RestController
@RequiredArgsConstructor
public class HealthController {

	private final HealthService service;

	/**
	 * 動いていれば 200（data は { "state": "UP" }）。データベースにつながらなければ 503（HealthServiceImpl が投げる）。
	 * 受け取る値がないので、RestInDto・ServiceInDto は作らない（CLAUDE.md 4.1）。
	 */
	@GetMapping("/api/health")
	public ApiResponse<HealthRestOutDto> health() {
		return ApiResponse.ok(service.check());
	}
}
