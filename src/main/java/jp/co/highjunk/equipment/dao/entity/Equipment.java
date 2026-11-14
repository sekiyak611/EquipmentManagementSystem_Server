package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Equipment {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long id;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String managementNo;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String managementNoActive;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long categoryId;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String maker;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String model;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String serialNo;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String size;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String resolution;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Boolean heightAdjustable;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Boolean pivot;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDate purchasedOn;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDate warrantyUntil;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String status;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDate disposedOn;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDate lastConfirmedOn;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String note;

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
    public String getManagementNo() {
        return managementNo;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setManagementNo(String managementNo) {
        this.managementNo = managementNo;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getManagementNoActive() {
        return managementNoActive;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setManagementNoActive(String managementNoActive) {
        this.managementNoActive = managementNoActive;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getCategoryId() {
        return categoryId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getMaker() {
        return maker;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setMaker(String maker) {
        this.maker = maker;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getModel() {
        return model;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setModel(String model) {
        this.model = model;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getSerialNo() {
        return serialNo;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getSize() {
        return size;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setSize(String size) {
        this.size = size;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getResolution() {
        return resolution;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Boolean getHeightAdjustable() {
        return heightAdjustable;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setHeightAdjustable(Boolean heightAdjustable) {
        this.heightAdjustable = heightAdjustable;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Boolean getPivot() {
        return pivot;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setPivot(Boolean pivot) {
        this.pivot = pivot;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDate getPurchasedOn() {
        return purchasedOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setPurchasedOn(LocalDate purchasedOn) {
        this.purchasedOn = purchasedOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDate getWarrantyUntil() {
        return warrantyUntil;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setWarrantyUntil(LocalDate warrantyUntil) {
        this.warrantyUntil = warrantyUntil;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getStatus() {
        return status;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setStatus(String status) {
        this.status = status;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDate getDisposedOn() {
        return disposedOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setDisposedOn(LocalDate disposedOn) {
        this.disposedOn = disposedOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDate getLastConfirmedOn() {
        return lastConfirmedOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setLastConfirmedOn(LocalDate lastConfirmedOn) {
        this.lastConfirmedOn = lastConfirmedOn;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getNote() {
        return note;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setNote(String note) {
        this.note = note;
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