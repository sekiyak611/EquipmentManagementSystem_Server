package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.time.LocalDate;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.Holiday;
import jp.co.highjunk.equipment.dao.entity.HolidayExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HolidayMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(HolidayExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(HolidayExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(LocalDate holidayOn);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(Holiday row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(Holiday row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<Holiday> selectByExample(HolidayExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    Holiday selectByPrimaryKey(LocalDate holidayOn);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") Holiday row, @Param("example") HolidayExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") Holiday row, @Param("example") HolidayExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKeySelective(Holiday row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKey(Holiday row);
}