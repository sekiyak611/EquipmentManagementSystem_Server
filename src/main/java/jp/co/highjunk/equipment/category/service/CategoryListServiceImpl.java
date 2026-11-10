package jp.co.highjunk.equipment.category.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.category.model.Category;
import jp.co.highjunk.equipment.category.model.CategoryEquipmentCount;
import jp.co.highjunk.equipment.dto.category.CategoryResponse;
import jp.co.highjunk.equipment.mapper.category.CategoryMapper;
import jp.co.highjunk.equipment.mapper.category.EquipmentMapper;

/**
 * K-01 種別の一覧の業務のルールの中身（詳細設計 category/K-01_種別一覧.md の4章）。
 * 種別と備品の数は、テーブルごとに読み、種別のIDで突き合わせる（JOIN を使わない。CLAUDE.md 5章）。
 */
@Service
public class CategoryListServiceImpl implements CategoryListService {

	private final CategoryMapper categoryMapper;

	private final EquipmentMapper equipmentMapper;

	public CategoryListServiceImpl(CategoryMapper categoryMapper, EquipmentMapper equipmentMapper) {
		this.categoryMapper = categoryMapper;
		this.equipmentMapper = equipmentMapper;
	}

	@Override
	public List<CategoryResponse> list() {
		List<Category> categories = categoryMapper.selectAll();
		if (categories.isEmpty()) {
			return List.of();
		}
		// 備品の数は、種別ごとに SQL を出さず、1回で全部の種別の分を読む（N+1 を防ぐ）
		Map<Long, Long> counts = equipmentMapper.countGroupByCategoryId().stream()
				.collect(Collectors.toMap(CategoryEquipmentCount::getCategoryId,
						CategoryEquipmentCount::getEquipmentCount));
		return categories.stream()
				.map(category -> new CategoryResponse(category.getId(), category.getName(), category.getInitial(),
						counts.getOrDefault(category.getId(), 0L), category.getVersion()))
				.toList();
	}
}
