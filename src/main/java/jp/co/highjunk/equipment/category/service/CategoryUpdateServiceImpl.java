package jp.co.highjunk.equipment.category.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.category.model.Category;
import jp.co.highjunk.equipment.category.validator.CategoryValidator;
import jp.co.highjunk.equipment.common.error.ConflictException;
import jp.co.highjunk.equipment.dto.category.CategoryResponse;
import jp.co.highjunk.equipment.dto.category.CategoryUpdateRequest;
import jp.co.highjunk.equipment.mapper.category.CategoryMapper;
import jp.co.highjunk.equipment.mapper.category.EquipmentMapper;

/**
 * K-03 種別を変えるの業務のルールの中身（詳細設計 category/K-03_種別を変える.md の4章）。
 */
@Service
public class CategoryUpdateServiceImpl implements CategoryUpdateService {

	private static final Logger log = LoggerFactory.getLogger(CategoryUpdateServiceImpl.class);

	private final CategoryMapper categoryMapper;

	private final EquipmentMapper equipmentMapper;

	private final CategoryValidator categoryValidator;

	private final Clock clock;

	public CategoryUpdateServiceImpl(CategoryMapper categoryMapper, EquipmentMapper equipmentMapper,
			CategoryValidator categoryValidator, Clock clock) {
		this.categoryMapper = categoryMapper;
		this.equipmentMapper = equipmentMapper;
		this.categoryValidator = categoryValidator;
		this.clock = clock;
	}

	@Override
	@Transactional
	public CategoryResponse update(long id, CategoryUpdateRequest request, long userId) {
		categoryValidator.validateUpdatable(id, request.version());
		categoryValidator.validateNotDuplicated(request.name(), request.initial(), id);

		Category category = new Category();
		category.setId(id);
		category.setName(request.name());
		category.setInitial(request.initial());
		category.setVersion(request.version());
		category.setUpdatedAt(LocalDateTime.now(clock));
		category.setUpdatedBy(userId);
		int updated;
		try {
			updated = categoryMapper.update(category);
		} catch (DuplicateKeyException e) {
			// 確かめたあとに、ほかの人が同じ名前・頭文字にしていた（K-03 の4章の8）
			throw CategoryDuplicateKeys.toFieldError(e);
		}
		if (updated == 0) {
			// 確かめたあとに、ほかの人が先に変えた・消した（K-03 の4章の8）
			throw new ConflictException(CategoryValidator.UPDATED_BY_OTHERS);
		}
		log.info("種別を変えました（id={}, name={}, initial={}）", id, request.name(), request.initial());
		return new CategoryResponse(id, request.name(), request.initial(), equipmentMapper.countByCategoryId(id),
				request.version() + 1);
	}
}
