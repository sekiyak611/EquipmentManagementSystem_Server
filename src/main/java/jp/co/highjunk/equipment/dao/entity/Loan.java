package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Loan {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long id;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long equipmentId;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long userId;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDate lentOn;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDate dueOn;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDate returnedOn;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String memo;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long activeEquipmentId;

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
    public Long getEquipmentId() {
        return equipmentId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getUserId() {
        return userId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDate getLentOn() {
        return lentOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setLentOn(LocalDate lentOn) {
        this.lentOn = lentOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDate getDueOn() {
        return dueOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setDueOn(LocalDate dueOn) {
        this.dueOn = dueOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDate getReturnedOn() {
        return returnedOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setReturnedOn(LocalDate returnedOn) {
        this.returnedOn = returnedOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getMemo() {
        return memo;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setMemo(String memo) {
        this.memo = memo;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getActiveEquipmentId() {
        return activeEquipmentId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setActiveEquipmentId(Long activeEquipmentId) {
        this.activeEquipmentId = activeEquipmentId;
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