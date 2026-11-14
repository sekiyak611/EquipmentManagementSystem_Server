package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDateTime;

public class PersistentLogin {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String series;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String username;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private String token;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    private LocalDateTime lastUsed;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getSeries() {
        return series;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setSeries(String series) {
        this.series = series;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getUsername() {
        return username;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setUsername(String username) {
        this.username = username;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getToken() {
        return token;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setToken(String token) {
        this.token = token;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public LocalDateTime getLastUsed() {
        return lastUsed;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }
}