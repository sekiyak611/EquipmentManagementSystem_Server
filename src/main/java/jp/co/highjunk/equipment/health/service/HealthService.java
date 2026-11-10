package jp.co.highjunk.equipment.health.service;

import jp.co.highjunk.equipment.common.error.ServiceUnavailableException;
import jp.co.highjunk.equipment.dto.health.HealthResponse;

/**
 * health 機能の業務のルール（A-08）の形。中身は HealthServiceImpl。
 * controller はこのインターフェースを使い、HealthServiceImpl を直接使わない（CLAUDE.md 5章）。
 */
public interface HealthService {

	/**
	 * データベースにつながるかを確かめる。
	 *
	 * @return 動いていれば state が UP
	 * @throws ServiceUnavailableException データベースにつながらないとき（503）
	 */
	HealthResponse check();
}
