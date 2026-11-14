package jp.co.highjunk.equipment.common.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

/**
 * mapper（MyBatis でデータベースを読み書きする部品）の見つけ方（詳細設計 common.md 4章）。
 * <p>
 * mapper の名前は `<テーブルの名前>Mapper`（CLAUDE.md 5章の命名規則）。どの機能もテーブルを直接読んでよいので、
 * mapper.auth.UserMapper と mapper.user.UserMapper のように、同じ名前の mapper がパッケージ違いでできる。
 * Spring の部品の名前（bean の名前）は、ふつうはクラスの名前（userMapper）なのでぶつかる。
 * そのため、部品の名前をパッケージ込みの名前（jp.co.highjunk.equipment.mapper.auth.UserMapper）にする。
 * <p>
 * 新しい形（CLAUDE.md 4.1）の Mapper は dao.mapper（MyBatis Generator が作る）と dao.mapper.custom（手で書く）にある。
 * 作り直しの途中は、古い mapper と新しい dao.mapper の両方を探す。dao.mapper.CategoryMapper と
 * mapper.category.CategoryMapper も、パッケージ込みの名前なのでぶつからない。
 */
@Configuration(proxyBeanMethods = false)
@MapperScan(basePackages = { "jp.co.highjunk.equipment.mapper", "jp.co.highjunk.equipment.dao.mapper" },
		annotationClass = Mapper.class,
		nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class MyBatisConfig {
}
