package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDateTime;

public class User {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long id;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String name;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String email;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String emailActive;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String passwordHash;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long roleId;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Integer failedLoginCount;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime lockedAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime sessionValidAfter;

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
    public String getEmail() {
        return email;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setEmail(String email) {
        this.email = email;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getEmailActive() {
        return emailActive;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setEmailActive(String emailActive) {
        this.emailActive = emailActive;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getPasswordHash() {
        return passwordHash;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getRoleId() {
        return roleId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Integer getFailedLoginCount() {
        return failedLoginCount;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setFailedLoginCount(Integer failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getSessionValidAfter() {
        return sessionValidAfter;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setSessionValidAfter(LocalDateTime sessionValidAfter) {
        this.sessionValidAfter = sessionValidAfter;
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