package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;

public class Permission {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String code;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String name;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String groupName;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Integer sortOrder;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getCode() {
        return code;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setCode(String code) {
        this.code = code;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getName() {
        return name;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setName(String name) {
        this.name = name;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getGroupName() {
        return groupName;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Integer getSortOrder() {
        return sortOrder;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}