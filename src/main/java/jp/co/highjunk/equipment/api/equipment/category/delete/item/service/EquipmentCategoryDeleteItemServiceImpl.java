package jp.co.highjunk.equipment.api.equipment.category.delete.item.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.error.NotFoundException;
import jp.co.highjunk.equipment.common.validation.ServiceInDtoChecker;
import jp.co.highjunk.equipment.dao.entity.Category;
import jp.co.highjunk.equipment.dao.entity.CategoryExample;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;

/**
 * K-04 種別を消すの業務の処理の中身（詳細設計 equipment/category/K-04_種別を消す.md の4章）。
 */
@Service
@RequiredArgsConstructor
public class EquipmentCategoryDeleteItemServiceImpl implements EquipmentCategoryDeleteItemService {

	private static final Logger log = LoggerFactory.getLogger(EquipmentCategoryDeleteItemServiceImpl.class);

	private final ServiceInDtoChecker serviceInDtoChecker;

	private final CategoryMapper categoryMapper;

	private final Clock clock;

	@Override
	@Transactional
	public void delete(EquipmentCategoryDeleteItemServiceInDto serviceInDto) {
		// 種別の行を押さえてから（FOR UPDATE）備品を数える（FOR SHARE）ので、確かめから消すまでの間に、その種別の備品を足されない（K-04 の8章）
		serviceInDtoChecker.check(serviceInDto);

		long id = serviceInDto.getId();
		Category category = categoryMapper.selectByPrimaryKey(id);
		LocalDateTime now = LocalDateTime.now(clock);
		Category row = new Category();
		row.setDeletedAt(now);
		row.setUpdatedAt(now);
		row.setUpdatedBy(serviceInDto.getLoginUserId());
		// 消すときも version を1増やす（K-04 の12章の No.1）
		row.setVersion(category.getVersion() + 1);

		CategoryExample example = new CategoryExample();
		example.createCriteria().andIdEqualTo(id).andDeletedAtIsNull();
		// 行を押さえているので、0件にはならない。念のため、0件なら見つからない扱いにする
		if (categoryMapper.updateByExampleSelective(row, example) == 0) {
			throw new NotFoundException();
		}
		log.info("種別を削除しました（id={}, name={}）", id, category.getName());
	}
}
