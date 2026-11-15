package jp.co.highjunk.equipment.dao.mapper.custom;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * users の、MyBatis Generator では作れない SQL（手で書く Mapper。CLAUDE.md 4.1・5章）。
 * SQL は src/main/resources/jp/co/highjunk/equipment/dao/mapper/custom/UserCustomMapper.xml に書く。
 */
@Mapper
public interface UserCustomMapper {

	/**
	 * 氏名にキーワードを含むユーザのID（削除済みも含める。C-01 の7章）。
	 * 大文字・小文字を区別せずに探すため LOWER を使い、LIKE の特別な文字を ! で打ち消す。Example では書けないので、手で書く。
	 *
	 * @param keywordPattern LIKE の形（%小文字のキーワード%。% _ ! には ! を付けたもの）
	 */
	List<Long> selectIdsByNameContaining(@Param("keywordPattern") String keywordPattern);

	/**
	 * パスワードを間違えた回数を1増やす（A-01 の4.1）。ロックされていない・削除されていない行だけ。
	 * 「読んで足して書く」だと、同時に間違えたときに回数が減ってしまうので、SQL の中で足す。
	 *
	 * @param id  ユーザのID
	 * @param now 更新日時（今。Clock から取ったもの）。updated_by と version は変えない（詳細設計 A-01 の12章）
	 * @return 増やした行の数（ロック済み・削除済みなら 0）
	 */
	int incrementFailedLoginCount(@Param("id") long id, @Param("now") LocalDateTime now);

	/**
	 * ユーザの行をロックして読む（FOR UPDATE。A-04）。同じ人への依頼が同時に来たとき、1つずつ処理するために使う。
	 * ロックは、呼んだ側の {@code @Transactional} が終わるまで続く。Example では書けないので、手で書く。
	 *
	 * @param id ユーザのID
	 * @return ユーザのID（いなければ null）
	 */
	Long selectIdForUpdate(@Param("id") long id);
}
