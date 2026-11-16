package jp.co.highjunk.equipment.api.equipment.mine.search.item.service;

import org.springframework.stereotype.Service;

import jp.co.highjunk.equipment.api.equipment.mine.search.item.model.EquipmentMineSearchItemRestOutDto;
import jp.co.highjunk.equipment.common.equipment.MyLoanListAssembler;
import lombok.RequiredArgsConstructor;

/**
 * B-01 自分が借りている備品の業務の処理の中身（詳細設計 equipment/mine/B-01_自分の備品の一覧.md の4章）。
 * 読むだけなので、@Transactional は付けない。一覧を作る処理は common.equipment.MyLoanListAssembler（B-04 と共通）。
 */
@Service
@RequiredArgsConstructor
public class EquipmentMineSearchItemServiceImpl implements EquipmentMineSearchItemService {

	private final MyLoanListAssembler myLoanListAssembler;

	@Override
	public EquipmentMineSearchItemRestOutDto search(EquipmentMineSearchItemServiceInDto serviceInDto) {
		return EquipmentMineSearchItemRestOutDto.from(myLoanListAssembler.assemble(serviceInDto.getLoginUserId()));
	}
}
