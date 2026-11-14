package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.PersistentLogin;
import jp.co.highjunk.equipment.dao.entity.PersistentLoginExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PersistentLoginMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(PersistentLoginExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(PersistentLoginExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(String series);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(PersistentLogin row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(PersistentLogin row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<PersistentLogin> selectByExample(PersistentLoginExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    PersistentLogin selectByPrimaryKey(String series);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") PersistentLogin row, @Param("example") PersistentLoginExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") PersistentLogin row, @Param("example") PersistentLoginExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKeySelective(PersistentLogin row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKey(PersistentLogin row);
}