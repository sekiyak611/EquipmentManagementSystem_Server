package jp.co.highjunk.equipment.api.health.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.api.health.model.HealthRestOutDto;
import jp.co.highjunk.equipment.common.constant.HealthState;
import jp.co.highjunk.equipment.common.error.ServiceUnavailableException;
import jp.co.highjunk.equipment.dao.mapper.custom.HealthCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * A-08 の業務の処理の中身。業務チェックがないので、Validator は作らない。
 */
@Service
@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {

	private static final Logger log = LoggerFactory.getLogger(HealthServiceImpl.class);

	private final HealthCustomMapper healthCustomMapper;

	@Override
	public HealthRestOutDto check() {
		try {
			healthCustomMapper.selectOne();
			return new HealthRestOutDto(HealthState.UP);
		} catch (DataAccessException e) {
			// 詳しい中身はログにだけ残す（docs/API設計.md 7章）
			log.warn("データベースにつながりません", e);
			throw new ServiceUnavailableException();
		}
	}
}
