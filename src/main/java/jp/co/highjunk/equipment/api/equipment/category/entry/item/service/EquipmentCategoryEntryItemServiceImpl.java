package jp.co.highjunk.equipment.api.equipment.category.entry.item.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.api.equipment.category.entry.item.model.EquipmentCategoryEntryItemRestOutDto;
import jp.co.highjunk.equipment.common.validation.CategoryChecks;
import jp.co.highjunk.equipment.common.validation.ServiceInDtoChecker;
import jp.co.highjunk.equipment.dao.entity.Category;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;

/**
 * K-02 種別を追加するの業務の処理の中身（詳細設計 equipment/category/K-02_種別を追加する.md の4章）。
 */
@Service
@RequiredArgsConstructor
public class EquipmentCategoryEntryItemServiceImpl implements EquipmentCategoryEntryItemService {

	private static final Logger log = LoggerFactory.getLogger(EquipmentCategoryEntryItemServiceImpl.class);

	private final ServiceInDtoChecker serviceInDtoChecker;

	private final CategoryMapper categoryMapper;

	private final Clock clock;

	@Override
	@Transactional
	public EquipmentCategoryEntryItemRestOutDto entry(EquipmentCategoryEntryItemServiceInDto serviceInDto) {
		serviceInDtoChecker.check(serviceInDto);

		LocalDateTime now = LocalDateTime.now(clock);
		Category category = new Category();
		category.setName(serviceInDto.getName());
		category.setInitial(serviceInDto.getInitial());
		category.setCreatedAt(now);
		category.setCreatedBy(serviceInDto.getLoginUserId());
		category.setUpdatedAt(now);
		category.setUpdatedBy(serviceInDto.getLoginUserId());
		try {
			// version は入れず、テーブルの初期値（0）にする（insertSelective は null の列を入れない）
			categoryMapper.insertSelective(category);
		} catch (DuplicateKeyException e) {
			// 確かめたあとに、ほかの人が同じ名前・頭文字の種別を足していた（K-02 の8章）
			throw CategoryChecks.duplicateKeyToFieldError(e);
		}
		log.info("種別を追加しました（id={}, name={}, initial={}）", category.getId(), category.getName(),
				category.getInitial());
		return new EquipmentCategoryEntryItemRestOutDto(category.getId(), category.getName(), category.getInitial(), 0,
				0);
	}
}
