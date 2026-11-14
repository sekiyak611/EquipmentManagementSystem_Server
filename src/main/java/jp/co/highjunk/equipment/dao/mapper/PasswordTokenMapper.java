package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.PasswordToken;
import jp.co.highjunk.equipment.dao.entity.PasswordTokenExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PasswordTokenMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(PasswordTokenExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(PasswordTokenExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(PasswordToken row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(PasswordToken row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<PasswordToken> selectByExample(PasswordTokenExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    PasswordToken selectByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") PasswordToken row, @Param("example") PasswordTokenExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") PasswordToken row, @Param("example") PasswordTokenExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKeySelective(PasswordToken row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKey(PasswordToken row);
}