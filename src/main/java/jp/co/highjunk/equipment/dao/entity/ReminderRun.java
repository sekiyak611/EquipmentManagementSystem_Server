package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDateTime;

public class ReminderRun {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long id;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String targetMonth;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime startedAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime finishedAt;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Integer successCount;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Integer failureCount;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getId() {
        return id;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setId(Long id) {
        this.id = id;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getTargetMonth() {
        return targetMonth;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setTargetMonth(String targetMonth) {
        this.targetMonth = targetMonth;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Integer getSuccessCount() {
        return successCount;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Integer getFailureCount() {
        return failureCount;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }
}