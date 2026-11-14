package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.ReminderLogItem;
import jp.co.highjunk.equipment.dao.entity.ReminderLogItemExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReminderLogItemMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(ReminderLogItemExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(ReminderLogItemExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(@Param("logId") Long logId, @Param("equipmentId") Long equipmentId);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(ReminderLogItem row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(ReminderLogItem row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<ReminderLogItem> selectByExample(ReminderLogItemExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") ReminderLogItem row, @Param("example") ReminderLogItemExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") ReminderLogItem row, @Param("example") ReminderLogItemExample example);
}