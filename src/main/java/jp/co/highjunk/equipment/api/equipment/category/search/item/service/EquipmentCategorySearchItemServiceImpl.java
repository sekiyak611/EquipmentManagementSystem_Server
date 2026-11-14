package jp.co.highjunk.equipment.api.equipment.category.search.item.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.api.equipment.category.search.item.model.EquipmentCategorySearchItemRestOutDto;
import jp.co.highjunk.equipment.dao.entity.Category;
import jp.co.highjunk.equipment.dao.entity.CategoryExample;
import jp.co.highjunk.equipment.dao.entity.custom.CategoryEquipmentCount;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import jp.co.highjunk.equipment.dao.mapper.custom.EquipmentCustomMapper;
import lombok.RequiredArgsConstructor;

/**
 * K-01 種別の一覧の業務の処理の中身（詳細設計 equipment/category/K-01_種別一覧.md の4章）。
 * 種別と備品の数は、テーブルごとに読み、種別のIDで突き合わせる（JOIN を使わない。CLAUDE.md 5章）。
 */
@Service
@RequiredArgsConstructor
public class EquipmentCategorySearchItemServiceImpl implements EquipmentCategorySearchItemService {

	/** 頭文字のある種別を先に頭文字の順、頭文字のないものを後ろに名前の順（画面設計 16章）。initial IS NULL は、ある行が 0、ない行が 1 */
	private static final String ORDER_BY = "`initial` IS NULL, `initial`, `name`";

	private final CategoryMapper categoryMapper;

	private final EquipmentCustomMapper equipmentCustomMapper;

	@Override
	public List<EquipmentCategorySearchItemRestOutDto> search() {
		CategoryExample example = new CategoryExample();
		example.createCriteria().andDeletedAtIsNull();
		example.setOrderByClause(ORDER_BY);
		List<Category> categories = categoryMapper.selectByExample(example);
		if (categories.isEmpty()) {
			return List.of();
		}
		// 備品の数は、種別ごとに SQL を出さず、1回で全部の種別の分を読む（N+1 を防ぐ）
		Map<Long, Long> counts = equipmentCustomMapper.countGroupByCategoryId().stream()
				.collect(Collectors.toMap(CategoryEquipmentCount::getCategoryId,
						CategoryEquipmentCount::getEquipmentCount));
		return categories.stream()
				.map(category -> new EquipmentCategorySearchItemRestOutDto(category.getId(), category.getName(),
						category.getInitial(), counts.getOrDefault(category.getId(), 0L), category.getVersion()))
				.toList();
	}
}
