package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.ReminderRun;
import jp.co.highjunk.equipment.dao.entity.ReminderRunExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReminderRunMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(ReminderRunExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(ReminderRunExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(ReminderRun row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(ReminderRun row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<ReminderRun> selectByExample(ReminderRunExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    ReminderRun selectByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") ReminderRun row, @Param("example") ReminderRunExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") ReminderRun row, @Param("example") ReminderRunExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKeySelective(ReminderRun row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKey(ReminderRun row);
}