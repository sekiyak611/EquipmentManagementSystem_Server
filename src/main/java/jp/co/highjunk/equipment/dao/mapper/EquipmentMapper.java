package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.Equipment;
import jp.co.highjunk.equipment.dao.entity.EquipmentExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EquipmentMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(EquipmentExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(EquipmentExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(Equipment row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(Equipment row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<Equipment> selectByExample(EquipmentExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    Equipment selectByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") Equipment row, @Param("example") EquipmentExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") Equipment row, @Param("example") EquipmentExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKeySelective(Equipment row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKey(Equipment row);
}