package jp.co.highjunk.equipment.health.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.common.error.ServiceUnavailableException;
import jp.co.highjunk.equipment.dto.health.HealthResponse;
import jp.co.highjunk.equipment.mapper.health.HealthMapper;

/**
 * health 機能の業務のルール（A-08）の中身。
 * 業務チェックがないため、Validator は使わない。
 */
@Service
public class HealthServiceImpl implements HealthService {

	private static final Logger log = LoggerFactory.getLogger(HealthServiceImpl.class);

	private final HealthMapper healthMapper;

	public HealthServiceImpl(HealthMapper healthMapper) {
		this.healthMapper = healthMapper;
	}

	@Override
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
