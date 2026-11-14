package jp.co.highjunk.equipment.api.equipment.management.entry.item.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.api.equipment.management.entry.item.model.EquipmentManagementEntryItemRestOutDto;
import jp.co.highjunk.equipment.api.equipment.management.entry.item.validator.EquipmentManagementEntryItemValidator;
import jp.co.highjunk.equipment.api.equipment.management.entry.item.validator.EquipmentManagementEntryItemValidatorDto;
import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatus;
import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.common.validation.EquipmentManagementNoNotDuplicated;
import jp.co.highjunk.equipment.common.validation.ServiceInDtoChecker;
import jp.co.highjunk.equipment.dao.entity.Category;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import jp.co.highjunk.equipment.dao.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;

/**
 * C-03 備品を登録するの業務の処理の中身（詳細設計 equipment/management/C-03_備品を登録する.md の4・5章）。
 */
@Service
@RequiredArgsConstructor
public class EquipmentManagementEntryItemServiceImpl implements EquipmentManagementEntryItemService {

	private static final Logger log = LoggerFactory.getLogger(EquipmentManagementEntryItemServiceImpl.class);

	/** 管理番号の重複禁止の名前（V1__create_tables.sql） */
	private static final String MANAGEMENT_NO_UNIQUE_KEY = "uk_equipment_management_no_active";

	private final ServiceInDtoChecker serviceInDtoChecker;

	private final EquipmentManagementEntryItemValidator validator;

	private final EquipmentMapper equipmentMapper;

	private final CategoryMapper categoryMapper;

	private final Clock clock;

	@Override
	@Transactional
	public EquipmentManagementEntryItemRestOutDto entry(EquipmentManagementEntryItemServiceInDto serviceInDto) {
		// 管理番号の重なりと種別。種別を FOR SHARE で読むので、確かめてから足すまでの間に種別を消されない（8章）
		serviceInDtoChecker.check(serviceInDto);

		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate today = now.toLocalDate();
		// 購入日・保証期日の決まり（今日の日付が要るので Validator。CLAUDE.md 5章）
		validator.validate(new EquipmentManagementEntryItemValidatorDto(serviceInDto.getPurchasedOn(),
				serviceInDto.getWarrantyUntil(), today));

		Equipment equipment = new Equipment();
		equipment.setManagementNo(serviceInDto.getManagementNo());
		equipment.setCategoryId(serviceInDto.getCategoryId());
		equipment.setMaker(serviceInDto.getMaker());
		equipment.setModel(serviceInDto.getModel());
		equipment.setSerialNo(serviceInDto.getSerialNo());
		equipment.setSize(serviceInDto.getSize());
		equipment.setResolution(serviceInDto.getResolution());
		equipment.setHeightAdjustable(serviceInDto.getHeightAdjustable());
		equipment.setPivot(serviceInDto.getPivot());
		equipment.setPurchasedOn(serviceInDto.getPurchasedOn());
		equipment.setWarrantyUntil(serviceInDto.getWarrantyUntil());
		equipment.setNote(serviceInDto.getNote());
		// 管理状態は管理中、最終確認日は登録した日（機能一覧 F-06、画面設計 12章）
		equipment.setStatus(EquipmentStatus.MANAGED.name());
		equipment.setLastConfirmedOn(today);
		equipment.setCreatedAt(now);
		equipment.setCreatedBy(serviceInDto.getLoginUserId());
		equipment.setUpdatedAt(now);
		equipment.setUpdatedBy(serviceInDto.getLoginUserId());
		try {
			// version は入れず、テーブルの初期値（0）にする（insertSelective は null の列を入れない）
			equipmentMapper.insertSelective(equipment);
		} catch (DuplicateKeyException e) {
			// 確かめたあとに、ほかの人が同じ管理番号の備品を足していた（8章）
			if (String.valueOf(e.getMessage()).contains(MANAGEMENT_NO_UNIQUE_KEY)) {
				throw new FieldErrorException("managementNo", EquipmentManagementNoNotDuplicated.MESSAGE);
			}
			throw e;
		}
		log.info("備品を登録しました（id={}, managementNo={}, categoryId={}）", equipment.getId(),
				equipment.getManagementNo(), equipment.getCategoryId());

		// 種別の行は FOR SHARE で押さえているので、ここで読んでも消されていない
		Category category = categoryMapper.selectByPrimaryKey(serviceInDto.getCategoryId());
		// 作ったばかりなので保管中で借用者はなく、最終確認日は今日（今月確認済み）
		return new EquipmentManagementEntryItemRestOutDto(equipment.getId(), equipment.getManagementNo(),
				category.getName(), equipment.getMaker(), equipment.getModel(), EquipmentStatus.MANAGED,
				LoanStatus.IN_STOCK, null, today, true);
	}
}
