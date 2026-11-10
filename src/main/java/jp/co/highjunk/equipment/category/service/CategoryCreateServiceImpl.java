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
import jp.co.highjunk.equipment.dto.category.CategoryCreateRequest;
import jp.co.highjunk.equipment.dto.category.CategoryResponse;
import jp.co.highjunk.equipment.mapper.category.CategoryMapper;

/**
 * K-02 種別を追加するの業務のルールの中身（詳細設計 category/K-02_種別を追加する.md の4章）。
 */
@Service
public class CategoryCreateServiceImpl implements CategoryCreateService {

	private static final Logger log = LoggerFactory.getLogger(CategoryCreateServiceImpl.class);

	private final CategoryMapper categoryMapper;

	private final CategoryValidator categoryValidator;

	private final Clock clock;

	public CategoryCreateServiceImpl(CategoryMapper categoryMapper, CategoryValidator categoryValidator, Clock clock) {
		this.categoryMapper = categoryMapper;
		this.categoryValidator = categoryValidator;
		this.clock = clock;
	}

	@Override
	@Transactional
	public CategoryResponse create(CategoryCreateRequest request, long userId) {
		categoryValidator.validateNotDuplicated(request.name(), request.initial(), null);

		LocalDateTime now = LocalDateTime.now(clock);
		Category category = new Category();
		category.setName(request.name());
		category.setInitial(request.initial());
		category.setCreatedAt(now);
		category.setCreatedBy(userId);
		category.setUpdatedAt(now);
		category.setUpdatedBy(userId);
		try {
			categoryMapper.insert(category);
		} catch (DuplicateKeyException e) {
			// 確かめたあとに、ほかの人が同じ名前・頭文字の種別を足していた（K-02 の8章）
			throw CategoryDuplicateKeys.toFieldError(e);
		}
		log.info("種別を追加しました（id={}, name={}, initial={}）", category.getId(), category.getName(),
				category.getInitial());
		return new CategoryResponse(category.getId(), category.getName(), category.getInitial(), 0, 0);
	}
}
