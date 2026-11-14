package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDateTime;

public class PasswordToken {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long id;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long userId;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String tokenHash;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String purpose;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime expiresAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime usedAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime revokedAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime createdAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getId() {
        return id;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setId(Long id) {
        this.id = id;
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
    public String getTokenHash() {
        return tokenHash;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getPurpose() {
        return purpose;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}