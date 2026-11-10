package jp.co.highjunk.equipment.category.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.category.model.Category;
import jp.co.highjunk.equipment.category.validator.CategoryValidator;
import jp.co.highjunk.equipment.common.error.NotFoundException;
import jp.co.highjunk.equipment.mapper.category.CategoryMapper;

/**
 * K-04 種別を消すの業務のルールの中身（詳細設計 category/K-04_種別を消す.md の4章）。
 */
@Service
public class CategoryDeleteServiceImpl implements CategoryDeleteService {

	private static final Logger log = LoggerFactory.getLogger(CategoryDeleteServiceImpl.class);

	private final CategoryMapper categoryMapper;

	private final CategoryValidator categoryValidator;

	private final Clock clock;

	public CategoryDeleteServiceImpl(CategoryMapper categoryMapper, CategoryValidator categoryValidator, Clock clock) {
		this.categoryMapper = categoryMapper;
		this.categoryValidator = categoryValidator;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void delete(long id, long userId) {
		Category category = categoryValidator.validateDeletable(id);
		// validateDeletable で行を押さえているので、0件にはならない。念のため、0件なら見つからない扱いにする
		if (categoryMapper.markDeleted(id, LocalDateTime.now(clock), userId) == 0) {
			throw new NotFoundException();
		}
		log.info("種別を削除しました（id={}, name={}）", id, category.getName());
	}
}
