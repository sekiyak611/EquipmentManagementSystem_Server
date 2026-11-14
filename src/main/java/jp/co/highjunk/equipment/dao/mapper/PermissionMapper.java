package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.Permission;
import jp.co.highjunk.equipment.dao.entity.PermissionExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PermissionMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(PermissionExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(PermissionExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(String code);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(Permission row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(Permission row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<Permission> selectByExample(PermissionExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    Permission selectByPrimaryKey(String code);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") Permission row, @Param("example") PermissionExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") Permission row, @Param("example") PermissionExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKeySelective(Permission row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKey(Permission row);
}