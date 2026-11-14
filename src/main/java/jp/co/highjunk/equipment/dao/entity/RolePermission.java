package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;

public class RolePermission {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long roleId;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String permissionCode;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getRoleId() {
        return roleId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getPermissionCode() {
        return permissionCode;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }
}