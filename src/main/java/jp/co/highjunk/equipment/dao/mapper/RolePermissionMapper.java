package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.RolePermission;
import jp.co.highjunk.equipment.dao.entity.RolePermissionExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RolePermissionMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(RolePermissionExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(RolePermissionExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(@Param("roleId") Long roleId, @Param("permissionCode") String permissionCode);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(RolePermission row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(RolePermission row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<RolePermission> selectByExample(RolePermissionExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") RolePermission row, @Param("example") RolePermissionExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") RolePermission row, @Param("example") RolePermissionExample example);
}