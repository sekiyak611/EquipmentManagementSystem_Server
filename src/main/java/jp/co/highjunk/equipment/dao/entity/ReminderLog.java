package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDateTime;

public class ReminderLog {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long id;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long runId;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long userId;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime sentAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String result;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String failureReason;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getId() {
        return id;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setId(Long id) {
        this.id = id;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getRunId() {
        return runId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setRunId(Long runId) {
        this.runId = runId;
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
    public LocalDateTime getSentAt() {
        return sentAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getResult() {
        return result;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setResult(String result) {
        this.result = result;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getFailureReason() {
        return failureReason;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}