package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDateTime;

public class Category {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long id;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String name;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String nameActive;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String initial;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String initialActive;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime deletedAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Integer version;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime createdAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long createdBy;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime updatedAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long updatedBy;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getId() {
        return id;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setId(Long id) {
        this.id = id;
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
    public String getNameActive() {
        return nameActive;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setNameActive(String nameActive) {
        this.nameActive = nameActive;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getInitial() {
        return initial;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setInitial(String initial) {
        this.initial = initial;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getInitialActive() {
        return initialActive;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setInitialActive(String initialActive) {
        this.initialActive = initialActive;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Integer getVersion() {
        return version;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getCreatedBy() {
        return createdBy;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getUpdatedBy() {
        return updatedBy;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}