package jp.co.highjunk.equipment.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import jp.co.highjunk.equipment.dao.entity.Category;
import jp.co.highjunk.equipment.dao.entity.CategoryExample;
import jp.co.highjunk.equipment.dao.entity.PermissionExample;
import jp.co.highjunk.equipment.dao.mapper.CategoryMapper;
import jp.co.highjunk.equipment.dao.mapper.PermissionMapper;

/**
 * MyBatis Generator が作った dao.mapper・dao.entity が、テスト用のデータベースで動くかのテスト（CLAUDE.md 4.1・5章）。
 * 作ったファイルそのものは手で直さないので、ここでは「見つけられる・読める・足せる」ことだけを確かめる。
 * 書き込むテストは、終わるたびに元に戻す（@Transactional。docs/テスト方針.md 4章）。
 */
@SpringBootTest
@Transactional
@DisplayName("MyBatis Generator が作った Mapper（dao.mapper）")
class DaoMapperTest {

	@Autowired
	ApplicationContext context;

	@Autowired
	CategoryMapper categoryMapper;

	@Autowired
	PermissionMapper permissionMapper;

	@Test
	@DisplayName("自動で作った Mapper も、手で書く Mapper も、パッケージ込みの名前で見つけられる")
	void beansAreFound() {
		assertThat(context.containsBean("jp.co.highjunk.equipment.dao.mapper.CategoryMapper")).isTrue();
		assertThat(context.containsBean("jp.co.highjunk.equipment.dao.mapper.custom.CategoryCustomMapper")).isTrue();
	}

	@Test
	@DisplayName("Example で数えられる（権限項目は最初のデータで18件）")
	void countByExample() {
		assertThat(permissionMapper.countByExample(new PermissionExample())).isEqualTo(18L);
	}

	@Test
	@DisplayName("足すと id が戻り、MySQL が作る列（name_active）にも値が入り、Criteria の条件で読める")
	void insertAndSelectByCriteria() {
		LocalDateTime now = LocalDateTime.of(2026, 11, 15, 10, 0);
		Category row = new Category();
		row.setName("テスト用の種別");
		row.setInitial("Z");
		row.setVersion(0);
		row.setCreatedAt(now);
		row.setUpdatedAt(now);

		assertThat(categoryMapper.insert(row)).isEqualTo(1);
		assertThat(row.getId()).isNotNull();

		Category saved = categoryMapper.selectByPrimaryKey(row.getId());
		assertThat(saved.getName()).isEqualTo("テスト用の種別");
		assertThat(saved.getNameActive()).isEqualTo("テスト用の種別");

		CategoryExample example = new CategoryExample();
		example.createCriteria().andNameEqualTo("テスト用の種別").andDeletedAtIsNull();
		List<Category> found = categoryMapper.selectByExample(example);
		assertThat(found).extracting(Category::getId).containsExactly(row.getId());
	}
}
