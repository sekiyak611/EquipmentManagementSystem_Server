package jp.co.highjunk.equipment.api.equipment.mine.update.item.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * B-04 で Service に渡す値（詳細設計 equipment/mine/B-04_確認した.md の2.5）。
 * Controller が、RestInDto の備品のID の一覧と、ログインしている人のユーザIDから作る（CLAUDE.md 5章）。
 * DB と照らす確かめ（あるか・自分が借りているか・管理中か）は、ServiceImpl が読んだ行が要るので、ここではなく Validator で行う（12章の No.6）。
 */
@Getter
@AllArgsConstructor
public class EquipmentMineUpdateItemServiceInDto {

	/** 確認する備品のID（送られたまま。重なりは、ServiceImpl が1つにまとめる） */
	private final List<Long> equipmentIds;

	/** ログインしている人のユーザID（借りた人との一致を確かめる。更新した人に入れる） */
	private final long loginUserId;
}
