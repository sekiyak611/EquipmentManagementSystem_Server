package jp.co.highjunk.equipment.api.equipment.management.entry.item.validator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jp.co.highjunk.equipment.common.error.ErrorItem;
import jp.co.highjunk.equipment.common.error.FieldErrorException;

/**
 * C-03 の業務チェック：購入日・保証期日の決まり（画面設計 12章。詳細設計 equipment/management/C-03_備品を登録する.md の5・6章）。
 * ServiceImpl が取った今日の日付が要るので、ServiceInDto のチェックではなく Validator で確かめる（CLAUDE.md 5章）。読むだけ。
 */
@Component
public class EquipmentManagementEntryItemValidator {

	/** 購入日が今日より後のときの文（画面設計 12章） */
	public static final String PURCHASED_ON_FUTURE = "購入日は今日以前の日付を入力してください";

	/** 保証期日が購入日より前のときの文（画面設計 12章） */
	public static final String WARRANTY_BEFORE_PURCHASE = "保証期日は購入日以降の日付を入力してください";

	/**
	 * 購入日は今日以前、保証期日は購入日以降。どちらかが空なら、その決まりは確かめない。
	 *
	 * @throws FieldErrorException 守れないとき（400。入力欄ごとの文。購入日・保証期日の順）
	 */
	public void validate(EquipmentManagementEntryItemValidatorDto validatorDto) {
		LocalDate purchasedOn = validatorDto.getPurchasedOn();
		LocalDate warrantyUntil = validatorDto.getWarrantyUntil();
		List<ErrorItem> errors = new ArrayList<>();
		if (purchasedOn != null && purchasedOn.isAfter(validatorDto.getToday())) {
			errors.add(ErrorItem.ofField("purchasedOn", PURCHASED_ON_FUTURE));
		}
		if (purchasedOn != null && warrantyUntil != null && warrantyUntil.isBefore(purchasedOn)) {
			errors.add(ErrorItem.ofField("warrantyUntil", WARRANTY_BEFORE_PURCHASE));
		}
		if (!errors.isEmpty()) {
			throw new FieldErrorException(errors);
		}
	}
}
