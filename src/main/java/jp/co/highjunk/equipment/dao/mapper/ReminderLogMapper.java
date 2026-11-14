package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.ReminderLog;
import jp.co.highjunk.equipment.dao.entity.ReminderLogExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReminderLogMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(ReminderLogExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(ReminderLogExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(ReminderLog row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(ReminderLog row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<ReminderLog> selectByExample(ReminderLogExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    ReminderLog selectByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") ReminderLog row, @Param("example") ReminderLogExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") ReminderLog row, @Param("example") ReminderLogExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKeySelective(ReminderLog row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKey(ReminderLog row);
}