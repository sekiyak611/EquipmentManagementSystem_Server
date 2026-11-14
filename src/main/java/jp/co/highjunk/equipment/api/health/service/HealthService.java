package jp.co.highjunk.equipment.api.health.service;

import jp.co.highjunk.equipment.api.health.model.HealthRestOutDto;
import jp.co.highjunk.equipment.common.error.ServiceUnavailableException;

/**
 * A-08 の業務の処理（中身は HealthServiceImpl。詳細設計 health/A-08_動いているかを答える.md）。
 */
public interface HealthService {

	/**
	 * データベースにつながるかを確かめる。
	 *
	 * @return 動いていれば state が UP
	 * @throws ServiceUnavailableException データベースにつながらないとき（503）
	 */
	HealthRestOutDto check();
}
