package jp.co.highjunk.equipment.api.equipment.category.update.item.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.api.equipment.category.update.item.model.EquipmentCategoryUpdateItemRestOutDto;
import jp.co.highjunk.equipment.common.error.ConflictException;
import jp.co.highjunk.equipment.common.validation.CategoryChecks;
import jp.co.highjunk.equipment.common.validation.CategoryVersionMatches;
import jp.co.highjunk.equipment.common.validation.ServiceInDtoChecker;
import jp.co.highjunk.equipment.dao.entity.Category;
import jp.co.highjunk.equipment.dao.entity.CategoryExample;
import jp.co.highjunk.equipment.dao.entity.EquipmentExample;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import jp.co.highjunk.equipment.dao.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;

/**
 * K-03 種別を変えるの業務の処理の中身（詳細設計 equipment/category/K-03_種別を変える.md の4章）。
 */
@Service
@RequiredArgsConstructor
public class EquipmentCategoryUpdateItemServiceImpl implements EquipmentCategoryUpdateItemService {

	private static final Logger log = LoggerFactory.getLogger(EquipmentCategoryUpdateItemServiceImpl.class);

	private final ServiceInDtoChecker serviceInDtoChecker;

	private final CategoryMapper categoryMapper;

	private final EquipmentMapper equipmentMapper;

	private final Clock clock;

	@Override
	@Transactional
	public EquipmentCategoryUpdateItemRestOutDto update(EquipmentCategoryUpdateItemServiceInDto serviceInDto) {
		serviceInDtoChecker.check(serviceInDto);

		long id = serviceInDto.getId();
		int version = serviceInDto.getVersion();
		// 作った日時・作った人は変えないので、今の行を読んでから、変える列だけを書き換える
		Category category = categoryMapper.selectByPrimaryKey(id);
		if (category == null) {
			// 確かめたあとに、ほかの人が消した（K-03 の4章の8）
			throw new ConflictException(CategoryVersionMatches.MESSAGE);
		}
		category.setName(serviceInDto.getName());
		category.setInitial(serviceInDto.getInitial());
		category.setVersion(version + 1);
		category.setUpdatedAt(LocalDateTime.now(clock));
		category.setUpdatedBy(serviceInDto.getLoginUserId());

		// 楽観ロック：画面を開いたときの version と同じで、削除されていない行だけを変える（データ設計 2章）
		CategoryExample example = new CategoryExample();
		example.createCriteria().andIdEqualTo(id).andVersionEqualTo(version).andDeletedAtIsNull();
		int updated;
		try {
			updated = categoryMapper.updateByExample(category, example);
		} catch (DuplicateKeyException e) {
			// 確かめたあとに、ほかの人が同じ名前・頭文字にしていた（K-03 の4章の8）
			throw CategoryChecks.duplicateKeyToFieldError(e);
		}
		if (updated == 0) {
			// 確かめたあとに、ほかの人が先に変えた・消した（K-03 の4章の8）
			throw new ConflictException(CategoryVersionMatches.MESSAGE);
		}
		log.info("種別を変えました（id={}, name={}, initial={}）", id, category.getName(), category.getInitial());

		EquipmentExample equipmentExample = new EquipmentExample();
		equipmentExample.createCriteria().andCategoryIdEqualTo(id).andDeletedAtIsNull();
		return new EquipmentCategoryUpdateItemRestOutDto(id, category.getName(), category.getInitial(),
				equipmentMapper.countByExample(equipmentExample), version + 1);
	}
}
