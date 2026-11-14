package jp.co.highjunk.equipment.common.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

/**
 * Mapper（MyBatis でデータベースを読み書きする部品）の見つけ方（詳細設計 common.md 4章）。
 * <p>
 * Mapper は dao.mapper（MyBatis Generator が作る）と dao.mapper.custom（手で書く）にある（CLAUDE.md 4.1）。
 * どちらも dao.mapper の下なので、dao.mapper の下の @Mapper を探す。
 * 部品の名前（bean の名前）は、パッケージ込みの名前（jp.co.highjunk.equipment.dao.mapper.UserMapper）にする。
 * 同じ名前の Mapper がパッケージ違いでできても、ぶつからないようにするため。
 */
@Configuration(proxyBeanMethods = false)
@MapperScan(basePackages = "jp.co.highjunk.equipment.dao.mapper", annotationClass = Mapper.class,
		nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class MyBatisConfig {
}
