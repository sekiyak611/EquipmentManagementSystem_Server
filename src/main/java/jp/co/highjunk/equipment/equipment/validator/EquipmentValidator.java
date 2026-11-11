package jp.co.highjunk.equipment.equipment.validator;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.constant.EquipmentStatus;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.common.error.ErrorItem;
import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.common.error.NotFoundException;
import jp.co.highjunk.equipment.dto.equipment.EquipmentCreateRequest;
import jp.co.highjunk.equipment.equipment.model.Category;
import jp.co.highjunk.equipment.equipment.model.Equipment;
import jp.co.highjunk.equipment.mapper.equipment.CategoryMapper;
import jp.co.highjunk.equipment.mapper.equipment.EquipmentMapper;

/**
 * 備品の業務チェック（詳細設計 equipment/C-03_備品を登録する.md・C-06_確認した.md の5・6章）。読むだけで、書き換えない（CLAUDE.md 5章）。
 * C-03 のあと、C-04・C-05・C-06 などでも使う予定なので、機能の名前でまとめる。
 */
@Component
public class EquipmentValidator {

	/** 管理番号が重なるときの文（画面設計 12章） */
	public static final String MANAGEMENT_NO_DUPLICATED = "この管理番号はすでに使われています";

	/** 種別がない・削除済みのときの文（画面設計 12章。2026-11-11 に人が決めた） */
	public static final String CATEGORY_REQUIRED = "種別を選んでください";

	/** 購入日が今日より後のときの文（画面設計 12章） */
	public static final String PURCHASED_ON_FUTURE = "購入日は今日以前の日付を入力してください";

	/** 保証期日が購入日より前のときの文（画面設計 12章） */
	public static final String WARRANTY_BEFORE_PURCHASE = "保証期日は購入日以降の日付を入力してください";

	/** 管理中でない備品を確認しようとしたときの文（画面設計 10章。2026-11-11 に人が決めた） */
	public static final String NOT_MANAGED = "管理中の備品だけ確認できます";

	private final EquipmentMapper equipmentMapper;

	private final CategoryMapper categoryMapper;

	private final Clock clock;

	public EquipmentValidator(EquipmentMapper equipmentMapper, CategoryMapper categoryMapper, Clock clock) {
		this.equipmentMapper = equipmentMapper;
		this.categoryMapper = categoryMapper;
		this.clock = clock;
	}

	/**
	 * 登録してよい備品かを確かめる（C-03）。種別は FOR SHARE で読むので、ServiceImpl の @Transactional の中で呼ぶ（8章）。
	 *
	 * @return 読んだ種別（返す値の種別の名前に使う）
	 * @throws FieldErrorException 守れないとき（400。入力欄ごとの文。管理番号・種別・購入日・保証期日の順）
	 */
	public Category validateCreatable(EquipmentCreateRequest request) {
		List<ErrorItem> errors = new ArrayList<>();
		if (equipmentMapper.countByManagementNo(request.managementNo()) > 0) {
			errors.add(ErrorItem.ofField("managementNo", MANAGEMENT_NO_DUPLICATED));
		}
		Category category = categoryMapper.selectByIdForShare(request.categoryId());
		if (category == null) {
			errors.add(ErrorItem.ofField("categoryId", CATEGORY_REQUIRED));
		}
		validateDates(request.purchasedOn(), request.warrantyUntil(), errors);
		if (!errors.isEmpty()) {
			throw new FieldErrorException(errors);
		}
		return category;
	}

	/**
	 * 確認してよい備品かを確かめる（C-06）。削除されていない備品があり、管理状態が管理中であること。
	 * 備品の行を押さえる（FOR UPDATE）ので、ServiceImpl の @Transactional の中で呼ぶ（C-06 の8章）。
	 *
	 * @return 読んだ備品
	 * @throws NotFoundException     ない・削除済みのとき（404）
	 * @throws BusinessRuleException 管理中でないとき（400）
	 */
	public Equipment validateConfirmable(long id) {
		Equipment equipment = equipmentMapper.selectByIdForUpdate(id);
		if (equipment == null) {
			throw new NotFoundException();
		}
		if (equipment.getStatus() != EquipmentStatus.MANAGED) {
			throw new BusinessRuleException(NOT_MANAGED);
		}
		return equipment;
	}

	/** 購入日は今日以前、保証期日は購入日以降（画面設計 12章）。どちらかが空なら、その決まりは確かめない */
	private void validateDates(LocalDate purchasedOn, LocalDate warrantyUntil, List<ErrorItem> errors) {
		if (purchasedOn != null && purchasedOn.isAfter(LocalDate.now(clock))) {
			errors.add(ErrorItem.ofField("purchasedOn", PURCHASED_ON_FUTURE));
		}
		if (purchasedOn != null && warrantyUntil != null && warrantyUntil.isBefore(purchasedOn)) {
			errors.add(ErrorItem.ofField("warrantyUntil", WARRANTY_BEFORE_PURCHASE));
		}
	}
}
