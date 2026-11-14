package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.Category;
import jp.co.highjunk.equipment.dao.entity.CategoryExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(CategoryExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(CategoryExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(Category row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(Category row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<Category> selectByExample(CategoryExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    Category selectByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") Category row, @Param("example") CategoryExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") Category row, @Param("example") CategoryExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKeySelective(Category row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKey(Category row);
}