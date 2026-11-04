package jp.co.highjunk.equipment.health.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.common.error.ServiceUnavailableException;
import jp.co.highjunk.equipment.dto.health.HealthResponse;
import jp.co.highjunk.equipment.mapper.health.HealthMapper;

/**
 * health 機能の業務のルール（A-08）。
 */
@Service
public class HealthService {

	private static final Logger log = LoggerFactory.getLogger(HealthService.class);

	private final HealthMapper healthMapper;

	public HealthService(HealthMapper healthMapper) {
		this.healthMapper = healthMapper;
	}

	/**
	 * データベースにつながるかを確かめる。
	 *
	 * @return 動いていれば state が UP
	 * @throws ServiceUnavailableException データベースにつながらないとき（503）
	 */
	public HealthResponse check() {
		try {
			healthMapper.selectOne();
			return HealthResponse.up();
		} catch (DataAccessException e) {
			// 詳しい中身はログにだけ残す（docs/API設計.md 7章）
			log.warn("データベースにつながりません", e);
			throw new ServiceUnavailableException();
		}
	}
}
