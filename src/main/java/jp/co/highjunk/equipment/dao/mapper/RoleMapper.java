package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.Role;
import jp.co.highjunk.equipment.dao.entity.RoleExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(RoleExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(RoleExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(Role row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(Role row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<Role> selectByExample(RoleExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    Role selectByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") Role row, @Param("example") RoleExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") Role row, @Param("example") RoleExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKeySelective(Role row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKey(Role row);
}