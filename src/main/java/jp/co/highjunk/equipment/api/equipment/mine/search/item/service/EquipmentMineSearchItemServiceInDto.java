package jp.co.highjunk.equipment.api.equipment.mine.search.item.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * B-01 で Service に渡す値（ログインしている人のユーザID だけ。条件は受け取らない）。
 * 自分の分だけを返すので、画面から借用者を受け取らない（IDOR を防ぐ。詳細設計 B-01 の10章）。
 */
@Getter
@AllArgsConstructor
public class EquipmentMineSearchItemServiceInDto {

	/** ログインしている人のユーザID */
	private final long loginUserId;
}
