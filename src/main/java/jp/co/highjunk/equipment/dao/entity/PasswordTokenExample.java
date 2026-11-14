package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PasswordTokenExample {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected String orderByClause;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected boolean distinct;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected List<Criteria> oredCriteria;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public PasswordTokenExample() {
        oredCriteria = new ArrayList<>();
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public String getOrderByClause() {
        return orderByClause;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public boolean isDistinct() {
        return distinct;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNull() {
            addCriterion("user_id is null");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualTo(Long value) {
            addCriterion("user_id =", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualTo(Long value) {
            addCriterion("user_id <>", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThan(Long value) {
            addCriterion("user_id >", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(Long value) {
            addCriterion("user_id >=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThan(Long value) {
            addCriterion("user_id <", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualTo(Long value) {
            addCriterion("user_id <=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdIn(List<Long> values) {
            addCriterion("user_id in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotIn(List<Long> values) {
            addCriterion("user_id not in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdBetween(Long value1, Long value2) {
            addCriterion("user_id between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotBetween(Long value1, Long value2) {
            addCriterion("user_id not between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andTokenHashIsNull() {
            addCriterion("token_hash is null");
            return (Criteria) this;
        }

        public Criteria andTokenHashIsNotNull() {
            addCriterion("token_hash is not null");
            return (Criteria) this;
        }

        public Criteria andTokenHashEqualTo(String value) {
            addCriterion("token_hash =", value, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashNotEqualTo(String value) {
            addCriterion("token_hash <>", value, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashGreaterThan(String value) {
            addCriterion("token_hash >", value, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashGreaterThanOrEqualTo(String value) {
            addCriterion("token_hash >=", value, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashLessThan(String value) {
            addCriterion("token_hash <", value, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashLessThanOrEqualTo(String value) {
            addCriterion("token_hash <=", value, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashLike(String value) {
            addCriterion("token_hash like", value, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashNotLike(String value) {
            addCriterion("token_hash not like", value, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashIn(List<String> values) {
            addCriterion("token_hash in", values, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashNotIn(List<String> values) {
            addCriterion("token_hash not in", values, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashBetween(String value1, String value2) {
            addCriterion("token_hash between", value1, value2, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andTokenHashNotBetween(String value1, String value2) {
            addCriterion("token_hash not between", value1, value2, "tokenHash");
            return (Criteria) this;
        }

        public Criteria andPurposeIsNull() {
            addCriterion("purpose is null");
            return (Criteria) this;
        }

        public Criteria andPurposeIsNotNull() {
            addCriterion("purpose is not null");
            return (Criteria) this;
        }

        public Criteria andPurposeEqualTo(String value) {
            addCriterion("purpose =", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeNotEqualTo(String value) {
            addCriterion("purpose <>", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeGreaterThan(String value) {
            addCriterion("purpose >", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeGreaterThanOrEqualTo(String value) {
            addCriterion("purpose >=", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeLessThan(String value) {
            addCriterion("purpose <", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeLessThanOrEqualTo(String value) {
            addCriterion("purpose <=", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeLike(String value) {
            addCriterion("purpose like", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeNotLike(String value) {
            addCriterion("purpose not like", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeIn(List<String> values) {
            addCriterion("purpose in", values, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeNotIn(List<String> values) {
            addCriterion("purpose not in", values, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeBetween(String value1, String value2) {
            addCriterion("purpose between", value1, value2, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeNotBetween(String value1, String value2) {
            addCriterion("purpose not between", value1, value2, "purpose");
            return (Criteria) this;
        }

        public Criteria andExpiresAtIsNull() {
            addCriterion("expires_at is null");
            return (Criteria) this;
        }

        public Criteria andExpiresAtIsNotNull() {
            addCriterion("expires_at is not null");
            return (Criteria) this;
        }

        public Criteria andExpiresAtEqualTo(LocalDateTime value) {
            addCriterion("expires_at =", value, "expiresAt");
            return (Criteria) this;
        }

        public Criteria andExpiresAtNotEqualTo(LocalDateTime value) {
            addCriterion("expires_at <>", value, "expiresAt");
            return (Criteria) this;
        }

        public Criteria andExpiresAtGreaterThan(LocalDateTime value) {
            addCriterion("expires_at >", value, "expiresAt");
            return (Criteria) this;
        }

        public Criteria andExpiresAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("expires_at >=", value, "expiresAt");
            return (Criteria) this;
        }

        public Criteria andExpiresAtLessThan(LocalDateTime value) {
            addCriterion("expires_at <", value, "expiresAt");
            return (Criteria) this;
        }

        public Criteria andExpiresAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("expires_at <=", value, "expiresAt");
            return (Criteria) this;
        }

        public Criteria andExpiresAtIn(List<LocalDateTime> values) {
            addCriterion("expires_at in", values, "expiresAt");
            return (Criteria) this;
        }

        public Criteria andExpiresAtNotIn(List<LocalDateTime> values) {
            addCriterion("expires_at not in", values, "expiresAt");
            return (Criteria) this;
        }

        public Criteria andExpiresAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("expires_at between", value1, value2, "expiresAt");
            return (Criteria) this;
        }

        public Criteria andExpiresAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("expires_at not between", value1, value2, "expiresAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtIsNull() {
            addCriterion("used_at is null");
            return (Criteria) this;
        }

        public Criteria andUsedAtIsNotNull() {
            addCriterion("used_at is not null");
            return (Criteria) this;
        }

        public Criteria andUsedAtEqualTo(LocalDateTime value) {
            addCriterion("used_at =", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtNotEqualTo(LocalDateTime value) {
            addCriterion("used_at <>", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtGreaterThan(LocalDateTime value) {
            addCriterion("used_at >", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("used_at >=", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtLessThan(LocalDateTime value) {
            addCriterion("used_at <", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("used_at <=", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtIn(List<LocalDateTime> values) {
            addCriterion("used_at in", values, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtNotIn(List<LocalDateTime> values) {
            addCriterion("used_at not in", values, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("used_at between", value1, value2, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("used_at not between", value1, value2, "usedAt");
            return (Criteria) this;
        }

        public Criteria andRevokedAtIsNull() {
            addCriterion("revoked_at is null");
            return (Criteria) this;
        }

        public Criteria andRevokedAtIsNotNull() {
            addCriterion("revoked_at is not null");
            return (Criteria) this;
        }

        public Criteria andRevokedAtEqualTo(LocalDateTime value) {
            addCriterion("revoked_at =", value, "revokedAt");
            return (Criteria) this;
        }

        public Criteria andRevokedAtNotEqualTo(LocalDateTime value) {
            addCriterion("revoked_at <>", value, "revokedAt");
            return (Criteria) this;
        }

        public Criteria andRevokedAtGreaterThan(LocalDateTime value) {
            addCriterion("revoked_at >", value, "revokedAt");
            return (Criteria) this;
        }

        public Criteria andRevokedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("revoked_at >=", value, "revokedAt");
            return (Criteria) this;
        }

        public Criteria andRevokedAtLessThan(LocalDateTime value) {
            addCriterion("revoked_at <", value, "revokedAt");
            return (Criteria) this;
        }

        public Criteria andRevokedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("revoked_at <=", value, "revokedAt");
            return (Criteria) this;
        }

        public Criteria andRevokedAtIn(List<LocalDateTime> values) {
            addCriterion("revoked_at in", values, "revokedAt");
            return (Criteria) this;
        }

        public Criteria andRevokedAtNotIn(List<LocalDateTime> values) {
            addCriterion("revoked_at not in", values, "revokedAt");
            return (Criteria) this;
        }

        public Criteria andRevokedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("revoked_at between", value1, value2, "revokedAt");
            return (Criteria) this;
        }

        public Criteria andRevokedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("revoked_at not between", value1, value2, "revokedAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNull() {
            addCriterion("created_at is null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNotNull() {
            addCriterion("created_at is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtEqualTo(LocalDateTime value) {
            addCriterion("created_at =", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotEqualTo(LocalDateTime value) {
            addCriterion("created_at <>", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThan(LocalDateTime value) {
            addCriterion("created_at >", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("created_at >=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThan(LocalDateTime value) {
            addCriterion("created_at <", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("created_at <=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIn(List<LocalDateTime> values) {
            addCriterion("created_at in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotIn(List<LocalDateTime> values) {
            addCriterion("created_at not in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("created_at between", value1, value2, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("created_at not between", value1, value2, "createdAt");
            return (Criteria) this;
        }
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="do_not_delete_during_merge")
    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}