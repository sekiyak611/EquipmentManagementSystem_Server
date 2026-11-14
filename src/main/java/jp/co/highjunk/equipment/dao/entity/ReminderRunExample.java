package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReminderRunExample {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected String orderByClause;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected boolean distinct;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected List<Criteria> oredCriteria;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public ReminderRunExample() {
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

        public Criteria andTargetMonthIsNull() {
            addCriterion("target_month is null");
            return (Criteria) this;
        }

        public Criteria andTargetMonthIsNotNull() {
            addCriterion("target_month is not null");
            return (Criteria) this;
        }

        public Criteria andTargetMonthEqualTo(String value) {
            addCriterion("target_month =", value, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthNotEqualTo(String value) {
            addCriterion("target_month <>", value, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthGreaterThan(String value) {
            addCriterion("target_month >", value, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthGreaterThanOrEqualTo(String value) {
            addCriterion("target_month >=", value, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthLessThan(String value) {
            addCriterion("target_month <", value, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthLessThanOrEqualTo(String value) {
            addCriterion("target_month <=", value, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthLike(String value) {
            addCriterion("target_month like", value, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthNotLike(String value) {
            addCriterion("target_month not like", value, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthIn(List<String> values) {
            addCriterion("target_month in", values, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthNotIn(List<String> values) {
            addCriterion("target_month not in", values, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthBetween(String value1, String value2) {
            addCriterion("target_month between", value1, value2, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andTargetMonthNotBetween(String value1, String value2) {
            addCriterion("target_month not between", value1, value2, "targetMonth");
            return (Criteria) this;
        }

        public Criteria andStartedAtIsNull() {
            addCriterion("started_at is null");
            return (Criteria) this;
        }

        public Criteria andStartedAtIsNotNull() {
            addCriterion("started_at is not null");
            return (Criteria) this;
        }

        public Criteria andStartedAtEqualTo(LocalDateTime value) {
            addCriterion("started_at =", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtNotEqualTo(LocalDateTime value) {
            addCriterion("started_at <>", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtGreaterThan(LocalDateTime value) {
            addCriterion("started_at >", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("started_at >=", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtLessThan(LocalDateTime value) {
            addCriterion("started_at <", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("started_at <=", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtIn(List<LocalDateTime> values) {
            addCriterion("started_at in", values, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtNotIn(List<LocalDateTime> values) {
            addCriterion("started_at not in", values, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("started_at between", value1, value2, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("started_at not between", value1, value2, "startedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtIsNull() {
            addCriterion("finished_at is null");
            return (Criteria) this;
        }

        public Criteria andFinishedAtIsNotNull() {
            addCriterion("finished_at is not null");
            return (Criteria) this;
        }

        public Criteria andFinishedAtEqualTo(LocalDateTime value) {
            addCriterion("finished_at =", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtNotEqualTo(LocalDateTime value) {
            addCriterion("finished_at <>", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtGreaterThan(LocalDateTime value) {
            addCriterion("finished_at >", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("finished_at >=", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtLessThan(LocalDateTime value) {
            addCriterion("finished_at <", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("finished_at <=", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtIn(List<LocalDateTime> values) {
            addCriterion("finished_at in", values, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtNotIn(List<LocalDateTime> values) {
            addCriterion("finished_at not in", values, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("finished_at between", value1, value2, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("finished_at not between", value1, value2, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andSuccessCountIsNull() {
            addCriterion("success_count is null");
            return (Criteria) this;
        }

        public Criteria andSuccessCountIsNotNull() {
            addCriterion("success_count is not null");
            return (Criteria) this;
        }

        public Criteria andSuccessCountEqualTo(Integer value) {
            addCriterion("success_count =", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountNotEqualTo(Integer value) {
            addCriterion("success_count <>", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountGreaterThan(Integer value) {
            addCriterion("success_count >", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("success_count >=", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountLessThan(Integer value) {
            addCriterion("success_count <", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountLessThanOrEqualTo(Integer value) {
            addCriterion("success_count <=", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountIn(List<Integer> values) {
            addCriterion("success_count in", values, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountNotIn(List<Integer> values) {
            addCriterion("success_count not in", values, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountBetween(Integer value1, Integer value2) {
            addCriterion("success_count between", value1, value2, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountNotBetween(Integer value1, Integer value2) {
            addCriterion("success_count not between", value1, value2, "successCount");
            return (Criteria) this;
        }

        public Criteria andFailureCountIsNull() {
            addCriterion("failure_count is null");
            return (Criteria) this;
        }

        public Criteria andFailureCountIsNotNull() {
            addCriterion("failure_count is not null");
            return (Criteria) this;
        }

        public Criteria andFailureCountEqualTo(Integer value) {
            addCriterion("failure_count =", value, "failureCount");
            return (Criteria) this;
        }

        public Criteria andFailureCountNotEqualTo(Integer value) {
            addCriterion("failure_count <>", value, "failureCount");
            return (Criteria) this;
        }

        public Criteria andFailureCountGreaterThan(Integer value) {
            addCriterion("failure_count >", value, "failureCount");
            return (Criteria) this;
        }

        public Criteria andFailureCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("failure_count >=", value, "failureCount");
            return (Criteria) this;
        }

        public Criteria andFailureCountLessThan(Integer value) {
            addCriterion("failure_count <", value, "failureCount");
            return (Criteria) this;
        }

        public Criteria andFailureCountLessThanOrEqualTo(Integer value) {
            addCriterion("failure_count <=", value, "failureCount");
            return (Criteria) this;
        }

        public Criteria andFailureCountIn(List<Integer> values) {
            addCriterion("failure_count in", values, "failureCount");
            return (Criteria) this;
        }

        public Criteria andFailureCountNotIn(List<Integer> values) {
            addCriterion("failure_count not in", values, "failureCount");
            return (Criteria) this;
        }

        public Criteria andFailureCountBetween(Integer value1, Integer value2) {
            addCriterion("failure_count between", value1, value2, "failureCount");
            return (Criteria) this;
        }

        public Criteria andFailureCountNotBetween(Integer value1, Integer value2) {
            addCriterion("failure_count not between", value1, value2, "failureCount");
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