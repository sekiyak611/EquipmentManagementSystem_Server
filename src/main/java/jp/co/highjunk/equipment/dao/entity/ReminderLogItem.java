package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;

public class ReminderLogItem {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long logId;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private Long equipmentId;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getLogId() {
        return logId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setLogId(Long logId) {
        this.logId = logId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Long getEquipmentId() {
        return equipmentId;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }
}