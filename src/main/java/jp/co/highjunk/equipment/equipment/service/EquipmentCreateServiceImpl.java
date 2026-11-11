package jp.co.highjunk.equipment.equipment.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.constant.LoanStatus;
import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.dto.equipment.EquipmentCreateRequest;
import jp.co.highjunk.equipment.dto.equipment.EquipmentListItem;
import jp.co.highjunk.equipment.equipment.model.Category;
import jp.co.highjunk.equipment.equipment.model.Equipment;
import jp.co.highjunk.equipment.equipment.validator.EquipmentValidator;
import jp.co.highjunk.equipment.mapper.equipment.EquipmentMapper;

/**
 * C-03 備品を登録するの業務のルールの中身（詳細設計 equipment/C-03_備品を登録する.md の4・5章）。
 */
@Service
public class EquipmentCreateServiceImpl implements EquipmentCreateService {

	private static final Logger log = LoggerFactory.getLogger(EquipmentCreateServiceImpl.class);

	/** 管理番号の重複禁止の名前（V1__create_tables.sql） */
	private static final String MANAGEMENT_NO_UNIQUE_KEY = "uk_equipment_management_no_active";

	private final EquipmentMapper equipmentMapper;

	private final EquipmentValidator equipmentValidator;

	private final Clock clock;

	public EquipmentCreateServiceImpl(EquipmentMapper equipmentMapper, EquipmentValidator equipmentValidator,
			Clock clock) {
		this.equipmentMapper = equipmentMapper;
		this.equipmentValidator = equipmentValidator;
		this.clock = clock;
	}

	@Override
	@Transactional
	public EquipmentListItem create(EquipmentCreateRequest request, long userId) {
		// 種別を FOR SHARE で読むので、確かめてから足すまでの間に種別を消されない（8章）
		Category category = equipmentValidator.validateCreatable(request);
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate today = now.toLocalDate();

		Equipment equipment = new Equipment();
		equipment.setManagementNo(request.managementNo());
		equipment.setCategoryId(request.categoryId());
		equipment.setMaker(request.maker());
		equipment.setModel(request.model());
		equipment.setSerialNo(request.serialNo());
		equipment.setSize(request.size());
		equipment.setResolution(request.resolution());
		equipment.setHeightAdjustable(request.heightAdjustable());
		equipment.setPivot(request.pivot());
		equipment.setPurchasedOn(request.purchasedOn());
		equipment.setWarrantyUntil(request.warrantyUntil());
		equipment.setNote(request.note());
		// 管理状態は管理中、最終確認日は登録した日（機能一覧 F-06、画面設計 12章）
		equipment.setStatus(EquipmentStatus.MANAGED);
		equipment.setLastConfirmedOn(today);
		equipment.setCreatedAt(now);
		equipment.setCreatedBy(userId);
		equipment.setUpdatedAt(now);
		equipment.setUpdatedBy(userId);
		try {
			equipmentMapper.insert(equipment);
		} catch (DuplicateKeyException e) {
			// 確かめたあとに、ほかの人が同じ管理番号の備品を足していた（8章）
			if (String.valueOf(e.getMessage()).contains(MANAGEMENT_NO_UNIQUE_KEY)) {
				throw new FieldErrorException("managementNo", EquipmentValidator.MANAGEMENT_NO_DUPLICATED);
			}
			throw e;
		}
		log.info("備品を登録しました（id={}, managementNo={}, categoryId={}）", equipment.getId(),
				equipment.getManagementNo(), equipment.getCategoryId());
		// 作ったばかりなので保管中で借用者はなく、最終確認日は今日（今月確認済み）
		return new EquipmentListItem(equipment.getId(), equipment.getManagementNo(), category.getName(),
				equipment.getMaker(), equipment.getModel(), EquipmentStatus.MANAGED, LoanStatus.IN_STOCK, null, today,
				true);
	}
}
