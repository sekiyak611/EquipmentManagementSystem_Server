package jp.co.highjunk.equipment.mapper.equipment;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.highjunk.equipment.equipment.model.User;

/**
 * equipment 機能の users テーブルの読み書き（読むだけ。詳細設計 equipment/equipment.md 2.1）。
 * auth 機能の mapper.auth.UserMapper とは別のもの。
 */
@Mapper
public interface UserMapper {

	/**
	 * 氏名にキーワードを含むユーザのID（削除済みも含める。C-01 の7章 No.2）。
	 *
	 * @param keywordPattern LIKE の形（%小文字のキーワード%。% _ ! には ! を付けたもの）
	 */
	List<Long> selectIdsByNameContaining(@Param("keywordPattern") String keywordPattern);

	/** ユーザをIDでまとめて読む（削除済みも読む。C-01 の7章 No.6）。ids は空にしない。 */
	List<User> selectByIds(@Param("ids") Collection<Long> ids);
}
