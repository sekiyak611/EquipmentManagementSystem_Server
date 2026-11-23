package jp.co.highjunk.equipment.dao.mapper.custom;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.dao.entity.Equipment;

/**
 * EquipmentCustomMapper の、B-04 で足したメソッド selectByIdsForUpdate・updateLastConfirmedOnByIds
 * （詳細設計 equipment/mine/B-04_確認した.md の7章 No.2・3）のテスト。
 * 書き込みは、テストが終わるたびに元に戻す（@Transactional）。行を押さえること（FOR UPDATE）は、CustomMapperBulkLockTest で確かめる。
 */
@SpringBootTest
@Transactional
@DisplayName("EquipmentCustomMapper（B-04 で足した、まとめて読む・更新するメソッド）")
class EquipmentCustomMapperTest {

	@Autowired
	EquipmentCustomMapper equipmentCustomMapper;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	Clock clock;

	@Test
	@DisplayName("selectByIdsForUpdate：送った ID の、削除されていない備品だけを、ID の昇順で返す（ない ID・削除済みは出ない）")
	void selectByIds() {
		long e3 = equipment("BE003");
		long e1 = equipment("BE001");
		long e2 = equipment("BE002");
		long deleted = equipment("BE004");
		long notAsked = equipment("BE005");
		jdbc.update("UPDATE equipment SET deleted_at = ? WHERE id = ?", LocalDateTime.now(clock), deleted);

		// 昇順でない順で送る（ORDER BY id を外すと、ここが崩れる）
		List<Equipment> rows = equipmentCustomMapper.selectByIdsForUpdate(List.of(e2, 999_999_999L, deleted, e1, e3));

		assertThat(rows).extracting(Equipment::getId).containsExactlyInAnyOrder(e3, e1, e2).isSorted();
		assertThat(rows).extracting(Equipment::getManagementNo).containsExactly("BE003", "BE001", "BE002");
		assertThat(rows).extracting(Equipment::getId).doesNotContain(notAsked, deleted);
		assertThat(rows).allSatisfy(row -> {
			assertThat(row.getStatus()).isEqualTo("MANAGED");
			assertThat(row.getVersion()).isEqualTo(0);
			assertThat(row.getDeletedAt()).isNull();
		});
	}

	@Test
	@DisplayName("selectByIdsForUpdate：ID の一覧が空・null なら、空を返す")
	void selectEmpty() {
		assertThat(equipmentCustomMapper.selectByIdsForUpdate(List.of())).isEmpty();
		assertThat(equipmentCustomMapper.selectByIdsForUpdate(null)).isEmpty();
	}

	@Test
	@DisplayName("updateLastConfirmedOnByIds：送った備品だけ、最終確認日・version+1（行ごとの version に1を足す）・更新日時・更新者を変え、件数を返す。ほかの備品・削除済みは変えない")
	void update() {
		LocalDate today = LocalDate.now(clock);
		LocalDateTime now = LocalDateTime.now(clock).withNano(0);
		long user = jdbc.queryForObject("SELECT id FROM users WHERE email = 'soumu@example.com'", Long.class);
		long e1 = equipment("BU001");
		long e2 = equipment("BU002");
		long deleted = equipment("BU003");
		long notAsked = equipment("BU004");
		jdbc.update("UPDATE equipment SET version = 5 WHERE id = ?", e2);
		jdbc.update("UPDATE equipment SET deleted_at = ? WHERE id = ?", now, deleted);
		Map<String, Object> notAskedBefore = row(notAsked);
		Map<String, Object> deletedBefore = row(deleted);
		Map<String, Object> e1Before = row(e1);

		int updated = equipmentCustomMapper.updateLastConfirmedOnByIds(List.of(e1, e2, deleted), today, now, user);

		assertThat(updated).isEqualTo(2);
		Map<String, Object> r1 = row(e1);
		assertThat(r1.get("last_confirmed_on").toString()).isEqualTo(today.toString());
		assertThat(((Number) r1.get("version")).intValue()).isEqualTo(1);
		assertThat(((Number) r1.get("updated_by")).longValue()).isEqualTo(user);
		Object updatedAt = r1.get("updated_at");
		assertThat(updatedAt instanceof LocalDateTime time ? time : ((java.sql.Timestamp) updatedAt).toLocalDateTime()).isEqualTo(now);
		// 変える4つの列のほかは、変わらない（管理番号・管理状態・種別・作った日時・作った人など）
		assertThat(withoutChanged(r1)).isEqualTo(withoutChanged(e1Before));
		assertThat(((Number) row(e2).get("version")).intValue()).isEqualTo(6);
		assertThat(row(notAsked)).isEqualTo(notAskedBefore);
		assertThat(row(deleted)).isEqualTo(deletedBefore);
	}

	@Test
	@DisplayName("updateLastConfirmedOnByIds：ID の一覧が空・null なら、何も更新せず 0 を返す")
	void updateEmpty() {
		LocalDate today = LocalDate.now(clock);
		LocalDateTime now = LocalDateTime.now(clock);
		assertThat(equipmentCustomMapper.updateLastConfirmedOnByIds(List.of(), today, now, 1L)).isEqualTo(0);
		assertThat(equipmentCustomMapper.updateLastConfirmedOnByIds(null, today, now, 1L)).isEqualTo(0);
	}

	Map<String, Object> row(long id) {
		return jdbc.queryForMap("SELECT * FROM equipment WHERE id = ?", id);
	}

	/** 更新で変わる4つの列（最終確認日・version・更新者・更新日時）を除いた、残りの列 */
	static Map<String, Object> withoutChanged(Map<String, Object> row) {
		Map<String, Object> copy = new java.util.HashMap<>(row);
		copy.keySet().removeAll(java.util.Set.of("last_confirmed_on", "version", "updated_by", "updated_at"));
		return copy;
	}

	long equipment(String managementNo) {
		LocalDateTime now = LocalDateTime.now(clock);
		jdbc.update("INSERT INTO equipment (management_no, category_id, status, created_at, updated_at)"
				+ " VALUES (?, 1, 'MANAGED', ?, ?)", managementNo, now, now);
		return jdbc.queryForObject("SELECT id FROM equipment WHERE management_no = ?", Long.class, managementNo);
	}
}
