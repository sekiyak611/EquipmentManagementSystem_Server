package jp.co.highjunk.equipment.dao.entity;

import jakarta.annotation.Generated;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EquipmentExample {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected String orderByClause;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected boolean distinct;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    protected List<Criteria> oredCriteria;

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    public EquipmentExample() {
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

        public Criteria andManagementNoIsNull() {
            addCriterion("management_no is null");
            return (Criteria) this;
        }

        public Criteria andManagementNoIsNotNull() {
            addCriterion("management_no is not null");
            return (Criteria) this;
        }

        public Criteria andManagementNoEqualTo(String value) {
            addCriterion("management_no =", value, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoNotEqualTo(String value) {
            addCriterion("management_no <>", value, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoGreaterThan(String value) {
            addCriterion("management_no >", value, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoGreaterThanOrEqualTo(String value) {
            addCriterion("management_no >=", value, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoLessThan(String value) {
            addCriterion("management_no <", value, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoLessThanOrEqualTo(String value) {
            addCriterion("management_no <=", value, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoLike(String value) {
            addCriterion("management_no like", value, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoNotLike(String value) {
            addCriterion("management_no not like", value, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoIn(List<String> values) {
            addCriterion("management_no in", values, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoNotIn(List<String> values) {
            addCriterion("management_no not in", values, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoBetween(String value1, String value2) {
            addCriterion("management_no between", value1, value2, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoNotBetween(String value1, String value2) {
            addCriterion("management_no not between", value1, value2, "managementNo");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveIsNull() {
            addCriterion("management_no_active is null");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveIsNotNull() {
            addCriterion("management_no_active is not null");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveEqualTo(String value) {
            addCriterion("management_no_active =", value, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveNotEqualTo(String value) {
            addCriterion("management_no_active <>", value, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveGreaterThan(String value) {
            addCriterion("management_no_active >", value, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveGreaterThanOrEqualTo(String value) {
            addCriterion("management_no_active >=", value, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveLessThan(String value) {
            addCriterion("management_no_active <", value, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveLessThanOrEqualTo(String value) {
            addCriterion("management_no_active <=", value, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveLike(String value) {
            addCriterion("management_no_active like", value, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveNotLike(String value) {
            addCriterion("management_no_active not like", value, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveIn(List<String> values) {
            addCriterion("management_no_active in", values, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveNotIn(List<String> values) {
            addCriterion("management_no_active not in", values, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveBetween(String value1, String value2) {
            addCriterion("management_no_active between", value1, value2, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andManagementNoActiveNotBetween(String value1, String value2) {
            addCriterion("management_no_active not between", value1, value2, "managementNoActive");
            return (Criteria) this;
        }

        public Criteria andCategoryIdIsNull() {
            addCriterion("category_id is null");
            return (Criteria) this;
        }

        public Criteria andCategoryIdIsNotNull() {
            addCriterion("category_id is not null");
            return (Criteria) this;
        }

        public Criteria andCategoryIdEqualTo(Long value) {
            addCriterion("category_id =", value, "categoryId");
            return (Criteria) this;
        }

        public Criteria andCategoryIdNotEqualTo(Long value) {
            addCriterion("category_id <>", value, "categoryId");
            return (Criteria) this;
        }

        public Criteria andCategoryIdGreaterThan(Long value) {
            addCriterion("category_id >", value, "categoryId");
            return (Criteria) this;
        }

        public Criteria andCategoryIdGreaterThanOrEqualTo(Long value) {
            addCriterion("category_id >=", value, "categoryId");
            return (Criteria) this;
        }

        public Criteria andCategoryIdLessThan(Long value) {
            addCriterion("category_id <", value, "categoryId");
            return (Criteria) this;
        }

        public Criteria andCategoryIdLessThanOrEqualTo(Long value) {
            addCriterion("category_id <=", value, "categoryId");
            return (Criteria) this;
        }

        public Criteria andCategoryIdIn(List<Long> values) {
            addCriterion("category_id in", values, "categoryId");
            return (Criteria) this;
        }

        public Criteria andCategoryIdNotIn(List<Long> values) {
            addCriterion("category_id not in", values, "categoryId");
            return (Criteria) this;
        }

        public Criteria andCategoryIdBetween(Long value1, Long value2) {
            addCriterion("category_id between", value1, value2, "categoryId");
            return (Criteria) this;
        }

        public Criteria andCategoryIdNotBetween(Long value1, Long value2) {
            addCriterion("category_id not between", value1, value2, "categoryId");
            return (Criteria) this;
        }

        public Criteria andMakerIsNull() {
            addCriterion("maker is null");
            return (Criteria) this;
        }

        public Criteria andMakerIsNotNull() {
            addCriterion("maker is not null");
            return (Criteria) this;
        }

        public Criteria andMakerEqualTo(String value) {
            addCriterion("maker =", value, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerNotEqualTo(String value) {
            addCriterion("maker <>", value, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerGreaterThan(String value) {
            addCriterion("maker >", value, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerGreaterThanOrEqualTo(String value) {
            addCriterion("maker >=", value, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerLessThan(String value) {
            addCriterion("maker <", value, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerLessThanOrEqualTo(String value) {
            addCriterion("maker <=", value, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerLike(String value) {
            addCriterion("maker like", value, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerNotLike(String value) {
            addCriterion("maker not like", value, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerIn(List<String> values) {
            addCriterion("maker in", values, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerNotIn(List<String> values) {
            addCriterion("maker not in", values, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerBetween(String value1, String value2) {
            addCriterion("maker between", value1, value2, "maker");
            return (Criteria) this;
        }

        public Criteria andMakerNotBetween(String value1, String value2) {
            addCriterion("maker not between", value1, value2, "maker");
            return (Criteria) this;
        }

        public Criteria andModelIsNull() {
            addCriterion("model is null");
            return (Criteria) this;
        }

        public Criteria andModelIsNotNull() {
            addCriterion("model is not null");
            return (Criteria) this;
        }

        public Criteria andModelEqualTo(String value) {
            addCriterion("model =", value, "model");
            return (Criteria) this;
        }

        public Criteria andModelNotEqualTo(String value) {
            addCriterion("model <>", value, "model");
            return (Criteria) this;
        }

        public Criteria andModelGreaterThan(String value) {
            addCriterion("model >", value, "model");
            return (Criteria) this;
        }

        public Criteria andModelGreaterThanOrEqualTo(String value) {
            addCriterion("model >=", value, "model");
            return (Criteria) this;
        }

        public Criteria andModelLessThan(String value) {
            addCriterion("model <", value, "model");
            return (Criteria) this;
        }

        public Criteria andModelLessThanOrEqualTo(String value) {
            addCriterion("model <=", value, "model");
            return (Criteria) this;
        }

        public Criteria andModelLike(String value) {
            addCriterion("model like", value, "model");
            return (Criteria) this;
        }

        public Criteria andModelNotLike(String value) {
            addCriterion("model not like", value, "model");
            return (Criteria) this;
        }

        public Criteria andModelIn(List<String> values) {
            addCriterion("model in", values, "model");
            return (Criteria) this;
        }

        public Criteria andModelNotIn(List<String> values) {
            addCriterion("model not in", values, "model");
            return (Criteria) this;
        }

        public Criteria andModelBetween(String value1, String value2) {
            addCriterion("model between", value1, value2, "model");
            return (Criteria) this;
        }

        public Criteria andModelNotBetween(String value1, String value2) {
            addCriterion("model not between", value1, value2, "model");
            return (Criteria) this;
        }

        public Criteria andSerialNoIsNull() {
            addCriterion("serial_no is null");
            return (Criteria) this;
        }

        public Criteria andSerialNoIsNotNull() {
            addCriterion("serial_no is not null");
            return (Criteria) this;
        }

        public Criteria andSerialNoEqualTo(String value) {
            addCriterion("serial_no =", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoNotEqualTo(String value) {
            addCriterion("serial_no <>", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoGreaterThan(String value) {
            addCriterion("serial_no >", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoGreaterThanOrEqualTo(String value) {
            addCriterion("serial_no >=", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoLessThan(String value) {
            addCriterion("serial_no <", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoLessThanOrEqualTo(String value) {
            addCriterion("serial_no <=", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoLike(String value) {
            addCriterion("serial_no like", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoNotLike(String value) {
            addCriterion("serial_no not like", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoIn(List<String> values) {
            addCriterion("serial_no in", values, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoNotIn(List<String> values) {
            addCriterion("serial_no not in", values, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoBetween(String value1, String value2) {
            addCriterion("serial_no between", value1, value2, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoNotBetween(String value1, String value2) {
            addCriterion("serial_no not between", value1, value2, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSizeIsNull() {
            addCriterion("`size` is null");
            return (Criteria) this;
        }

        public Criteria andSizeIsNotNull() {
            addCriterion("`size` is not null");
            return (Criteria) this;
        }

        public Criteria andSizeEqualTo(String value) {
            addCriterion("`size` =", value, "size");
            return (Criteria) this;
        }

        public Criteria andSizeNotEqualTo(String value) {
            addCriterion("`size` <>", value, "size");
            return (Criteria) this;
        }

        public Criteria andSizeGreaterThan(String value) {
            addCriterion("`size` >", value, "size");
            return (Criteria) this;
        }

        public Criteria andSizeGreaterThanOrEqualTo(String value) {
            addCriterion("`size` >=", value, "size");
            return (Criteria) this;
        }

        public Criteria andSizeLessThan(String value) {
            addCriterion("`size` <", value, "size");
            return (Criteria) this;
        }

        public Criteria andSizeLessThanOrEqualTo(String value) {
            addCriterion("`size` <=", value, "size");
            return (Criteria) this;
        }

        public Criteria andSizeLike(String value) {
            addCriterion("`size` like", value, "size");
            return (Criteria) this;
        }

        public Criteria andSizeNotLike(String value) {
            addCriterion("`size` not like", value, "size");
            return (Criteria) this;
        }

        public Criteria andSizeIn(List<String> values) {
            addCriterion("`size` in", values, "size");
            return (Criteria) this;
        }

        public Criteria andSizeNotIn(List<String> values) {
            addCriterion("`size` not in", values, "size");
            return (Criteria) this;
        }

        public Criteria andSizeBetween(String value1, String value2) {
            addCriterion("`size` between", value1, value2, "size");
            return (Criteria) this;
        }

        public Criteria andSizeNotBetween(String value1, String value2) {
            addCriterion("`size` not between", value1, value2, "size");
            return (Criteria) this;
        }

        public Criteria andResolutionIsNull() {
            addCriterion("resolution is null");
            return (Criteria) this;
        }

        public Criteria andResolutionIsNotNull() {
            addCriterion("resolution is not null");
            return (Criteria) this;
        }

        public Criteria andResolutionEqualTo(String value) {
            addCriterion("resolution =", value, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionNotEqualTo(String value) {
            addCriterion("resolution <>", value, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionGreaterThan(String value) {
            addCriterion("resolution >", value, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionGreaterThanOrEqualTo(String value) {
            addCriterion("resolution >=", value, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionLessThan(String value) {
            addCriterion("resolution <", value, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionLessThanOrEqualTo(String value) {
            addCriterion("resolution <=", value, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionLike(String value) {
            addCriterion("resolution like", value, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionNotLike(String value) {
            addCriterion("resolution not like", value, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionIn(List<String> values) {
            addCriterion("resolution in", values, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionNotIn(List<String> values) {
            addCriterion("resolution not in", values, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionBetween(String value1, String value2) {
            addCriterion("resolution between", value1, value2, "resolution");
            return (Criteria) this;
        }

        public Criteria andResolutionNotBetween(String value1, String value2) {
            addCriterion("resolution not between", value1, value2, "resolution");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableIsNull() {
            addCriterion("height_adjustable is null");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableIsNotNull() {
            addCriterion("height_adjustable is not null");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableEqualTo(Boolean value) {
            addCriterion("height_adjustable =", value, "heightAdjustable");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableNotEqualTo(Boolean value) {
            addCriterion("height_adjustable <>", value, "heightAdjustable");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableGreaterThan(Boolean value) {
            addCriterion("height_adjustable >", value, "heightAdjustable");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableGreaterThanOrEqualTo(Boolean value) {
            addCriterion("height_adjustable >=", value, "heightAdjustable");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableLessThan(Boolean value) {
            addCriterion("height_adjustable <", value, "heightAdjustable");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableLessThanOrEqualTo(Boolean value) {
            addCriterion("height_adjustable <=", value, "heightAdjustable");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableIn(List<Boolean> values) {
            addCriterion("height_adjustable in", values, "heightAdjustable");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableNotIn(List<Boolean> values) {
            addCriterion("height_adjustable not in", values, "heightAdjustable");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableBetween(Boolean value1, Boolean value2) {
            addCriterion("height_adjustable between", value1, value2, "heightAdjustable");
            return (Criteria) this;
        }

        public Criteria andHeightAdjustableNotBetween(Boolean value1, Boolean value2) {
            addCriterion("height_adjustable not between", value1, value2, "heightAdjustable");
            return (Criteria) this;
        }

        public Criteria andPivotIsNull() {
            addCriterion("pivot is null");
            return (Criteria) this;
        }

        public Criteria andPivotIsNotNull() {
            addCriterion("pivot is not null");
            return (Criteria) this;
        }

        public Criteria andPivotEqualTo(Boolean value) {
            addCriterion("pivot =", value, "pivot");
            return (Criteria) this;
        }

        public Criteria andPivotNotEqualTo(Boolean value) {
            addCriterion("pivot <>", value, "pivot");
            return (Criteria) this;
        }

        public Criteria andPivotGreaterThan(Boolean value) {
            addCriterion("pivot >", value, "pivot");
            return (Criteria) this;
        }

        public Criteria andPivotGreaterThanOrEqualTo(Boolean value) {
            addCriterion("pivot >=", value, "pivot");
            return (Criteria) this;
        }

        public Criteria andPivotLessThan(Boolean value) {
            addCriterion("pivot <", value, "pivot");
            return (Criteria) this;
        }

        public Criteria andPivotLessThanOrEqualTo(Boolean value) {
            addCriterion("pivot <=", value, "pivot");
            return (Criteria) this;
        }

        public Criteria andPivotIn(List<Boolean> values) {
            addCriterion("pivot in", values, "pivot");
            return (Criteria) this;
        }

        public Criteria andPivotNotIn(List<Boolean> values) {
            addCriterion("pivot not in", values, "pivot");
            return (Criteria) this;
        }

        public Criteria andPivotBetween(Boolean value1, Boolean value2) {
            addCriterion("pivot between", value1, value2, "pivot");
            return (Criteria) this;
        }

        public Criteria andPivotNotBetween(Boolean value1, Boolean value2) {
            addCriterion("pivot not between", value1, value2, "pivot");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnIsNull() {
            addCriterion("purchased_on is null");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnIsNotNull() {
            addCriterion("purchased_on is not null");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnEqualTo(LocalDate value) {
            addCriterion("purchased_on =", value, "purchasedOn");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnNotEqualTo(LocalDate value) {
            addCriterion("purchased_on <>", value, "purchasedOn");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnGreaterThan(LocalDate value) {
            addCriterion("purchased_on >", value, "purchasedOn");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnGreaterThanOrEqualTo(LocalDate value) {
            addCriterion("purchased_on >=", value, "purchasedOn");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnLessThan(LocalDate value) {
            addCriterion("purchased_on <", value, "purchasedOn");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnLessThanOrEqualTo(LocalDate value) {
            addCriterion("purchased_on <=", value, "purchasedOn");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnIn(List<LocalDate> values) {
            addCriterion("purchased_on in", values, "purchasedOn");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnNotIn(List<LocalDate> values) {
            addCriterion("purchased_on not in", values, "purchasedOn");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnBetween(LocalDate value1, LocalDate value2) {
            addCriterion("purchased_on between", value1, value2, "purchasedOn");
            return (Criteria) this;
        }

        public Criteria andPurchasedOnNotBetween(LocalDate value1, LocalDate value2) {
            addCriterion("purchased_on not between", value1, value2, "purchasedOn");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilIsNull() {
            addCriterion("warranty_until is null");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilIsNotNull() {
            addCriterion("warranty_until is not null");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilEqualTo(LocalDate value) {
            addCriterion("warranty_until =", value, "warrantyUntil");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilNotEqualTo(LocalDate value) {
            addCriterion("warranty_until <>", value, "warrantyUntil");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilGreaterThan(LocalDate value) {
            addCriterion("warranty_until >", value, "warrantyUntil");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilGreaterThanOrEqualTo(LocalDate value) {
            addCriterion("warranty_until >=", value, "warrantyUntil");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilLessThan(LocalDate value) {
            addCriterion("warranty_until <", value, "warrantyUntil");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilLessThanOrEqualTo(LocalDate value) {
            addCriterion("warranty_until <=", value, "warrantyUntil");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilIn(List<LocalDate> values) {
            addCriterion("warranty_until in", values, "warrantyUntil");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilNotIn(List<LocalDate> values) {
            addCriterion("warranty_until not in", values, "warrantyUntil");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilBetween(LocalDate value1, LocalDate value2) {
            addCriterion("warranty_until between", value1, value2, "warrantyUntil");
            return (Criteria) this;
        }

        public Criteria andWarrantyUntilNotBetween(LocalDate value1, LocalDate value2) {
            addCriterion("warranty_until not between", value1, value2, "warrantyUntil");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("`status` is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("`status` is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("`status` like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("`status` not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andDisposedOnIsNull() {
            addCriterion("disposed_on is null");
            return (Criteria) this;
        }

        public Criteria andDisposedOnIsNotNull() {
            addCriterion("disposed_on is not null");
            return (Criteria) this;
        }

        public Criteria andDisposedOnEqualTo(LocalDate value) {
            addCriterion("disposed_on =", value, "disposedOn");
            return (Criteria) this;
        }

        public Criteria andDisposedOnNotEqualTo(LocalDate value) {
            addCriterion("disposed_on <>", value, "disposedOn");
            return (Criteria) this;
        }

        public Criteria andDisposedOnGreaterThan(LocalDate value) {
            addCriterion("disposed_on >", value, "disposedOn");
            return (Criteria) this;
        }

        public Criteria andDisposedOnGreaterThanOrEqualTo(LocalDate value) {
            addCriterion("disposed_on >=", value, "disposedOn");
            return (Criteria) this;
        }

        public Criteria andDisposedOnLessThan(LocalDate value) {
            addCriterion("disposed_on <", value, "disposedOn");
            return (Criteria) this;
        }

        public Criteria andDisposedOnLessThanOrEqualTo(LocalDate value) {
            addCriterion("disposed_on <=", value, "disposedOn");
            return (Criteria) this;
        }

        public Criteria andDisposedOnIn(List<LocalDate> values) {
            addCriterion("disposed_on in", values, "disposedOn");
            return (Criteria) this;
        }

        public Criteria andDisposedOnNotIn(List<LocalDate> values) {
            addCriterion("disposed_on not in", values, "disposedOn");
            return (Criteria) this;
        }

        public Criteria andDisposedOnBetween(LocalDate value1, LocalDate value2) {
            addCriterion("disposed_on between", value1, value2, "disposedOn");
            return (Criteria) this;
        }

        public Criteria andDisposedOnNotBetween(LocalDate value1, LocalDate value2) {
            addCriterion("disposed_on not between", value1, value2, "disposedOn");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnIsNull() {
            addCriterion("last_confirmed_on is null");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnIsNotNull() {
            addCriterion("last_confirmed_on is not null");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnEqualTo(LocalDate value) {
            addCriterion("last_confirmed_on =", value, "lastConfirmedOn");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnNotEqualTo(LocalDate value) {
            addCriterion("last_confirmed_on <>", value, "lastConfirmedOn");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnGreaterThan(LocalDate value) {
            addCriterion("last_confirmed_on >", value, "lastConfirmedOn");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnGreaterThanOrEqualTo(LocalDate value) {
            addCriterion("last_confirmed_on >=", value, "lastConfirmedOn");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnLessThan(LocalDate value) {
            addCriterion("last_confirmed_on <", value, "lastConfirmedOn");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnLessThanOrEqualTo(LocalDate value) {
            addCriterion("last_confirmed_on <=", value, "lastConfirmedOn");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnIn(List<LocalDate> values) {
            addCriterion("last_confirmed_on in", values, "lastConfirmedOn");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnNotIn(List<LocalDate> values) {
            addCriterion("last_confirmed_on not in", values, "lastConfirmedOn");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnBetween(LocalDate value1, LocalDate value2) {
            addCriterion("last_confirmed_on between", value1, value2, "lastConfirmedOn");
            return (Criteria) this;
        }

        public Criteria andLastConfirmedOnNotBetween(LocalDate value1, LocalDate value2) {
            addCriterion("last_confirmed_on not between", value1, value2, "lastConfirmedOn");
            return (Criteria) this;
        }

        public Criteria andNoteIsNull() {
            addCriterion("note is null");
            return (Criteria) this;
        }

        public Criteria andNoteIsNotNull() {
            addCriterion("note is not null");
            return (Criteria) this;
        }

        public Criteria andNoteEqualTo(String value) {
            addCriterion("note =", value, "note");
            return (Criteria) this;
        }

        public Criteria andNoteNotEqualTo(String value) {
            addCriterion("note <>", value, "note");
            return (Criteria) this;
        }

        public Criteria andNoteGreaterThan(String value) {
            addCriterion("note >", value, "note");
            return (Criteria) this;
        }

        public Criteria andNoteGreaterThanOrEqualTo(String value) {
            addCriterion("note >=", value, "note");
            return (Criteria) this;
        }

        public Criteria andNoteLessThan(String value) {
            addCriterion("note <", value, "note");
            return (Criteria) this;
        }

        public Criteria andNoteLessThanOrEqualTo(String value) {
            addCriterion("note <=", value, "note");
            return (Criteria) this;
        }

        public Criteria andNoteLike(String value) {
            addCriterion("note like", value, "note");
            return (Criteria) this;
        }

        public Criteria andNoteNotLike(String value) {
            addCriterion("note not like", value, "note");
            return (Criteria) this;
        }

        public Criteria andNoteIn(List<String> values) {
            addCriterion("note in", values, "note");
            return (Criteria) this;
        }

        public Criteria andNoteNotIn(List<String> values) {
            addCriterion("note not in", values, "note");
            return (Criteria) this;
        }

        public Criteria andNoteBetween(String value1, String value2) {
            addCriterion("note between", value1, value2, "note");
            return (Criteria) this;
        }

        public Criteria andNoteNotBetween(String value1, String value2) {
            addCriterion("note not between", value1, value2, "note");
            return (Criteria) this;
        }

        public Criteria andDeletedAtIsNull() {
            addCriterion("deleted_at is null");
            return (Criteria) this;
        }

        public Criteria andDeletedAtIsNotNull() {
            addCriterion("deleted_at is not null");
            return (Criteria) this;
        }

        public Criteria andDeletedAtEqualTo(LocalDateTime value) {
            addCriterion("deleted_at =", value, "deletedAt");
            return (Criteria) this;
        }

        public Criteria andDeletedAtNotEqualTo(LocalDateTime value) {
            addCriterion("deleted_at <>", value, "deletedAt");
            return (Criteria) this;
        }

        public Criteria andDeletedAtGreaterThan(LocalDateTime value) {
            addCriterion("deleted_at >", value, "deletedAt");
            return (Criteria) this;
        }

        public Criteria andDeletedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("deleted_at >=", value, "deletedAt");
            return (Criteria) this;
        }

        public Criteria andDeletedAtLessThan(LocalDateTime value) {
            addCriterion("deleted_at <", value, "deletedAt");
            return (Criteria) this;
        }

        public Criteria andDeletedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("deleted_at <=", value, "deletedAt");
            return (Criteria) this;
        }

        public Criteria andDeletedAtIn(List<LocalDateTime> values) {
            addCriterion("deleted_at in", values, "deletedAt");
            return (Criteria) this;
        }

        public Criteria andDeletedAtNotIn(List<LocalDateTime> values) {
            addCriterion("deleted_at not in", values, "deletedAt");
            return (Criteria) this;
        }

        public Criteria andDeletedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("deleted_at between", value1, value2, "deletedAt");
            return (Criteria) this;
        }

        public Criteria andDeletedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("deleted_at not between", value1, value2, "deletedAt");
            return (Criteria) this;
        }

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(Integer value) {
            addCriterion("version =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(Integer value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(Integer value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(Integer value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(Integer value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<Integer> values) {
            addCriterion("version in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<Integer> values) {
            addCriterion("version not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(Integer value1, Integer value2) {
            addCriterion("version between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("version not between", value1, value2, "version");
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

        public Criteria andCreatedByIsNull() {
            addCriterion("created_by is null");
            return (Criteria) this;
        }

        public Criteria andCreatedByIsNotNull() {
            addCriterion("created_by is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedByEqualTo(Long value) {
            addCriterion("created_by =", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotEqualTo(Long value) {
            addCriterion("created_by <>", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByGreaterThan(Long value) {
            addCriterion("created_by >", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByGreaterThanOrEqualTo(Long value) {
            addCriterion("created_by >=", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByLessThan(Long value) {
            addCriterion("created_by <", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByLessThanOrEqualTo(Long value) {
            addCriterion("created_by <=", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByIn(List<Long> values) {
            addCriterion("created_by in", values, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotIn(List<Long> values) {
            addCriterion("created_by not in", values, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByBetween(Long value1, Long value2) {
            addCriterion("created_by between", value1, value2, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotBetween(Long value1, Long value2) {
            addCriterion("created_by not between", value1, value2, "createdBy");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNull() {
            addCriterion("updated_at is null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNotNull() {
            addCriterion("updated_at is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtEqualTo(LocalDateTime value) {
            addCriterion("updated_at =", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotEqualTo(LocalDateTime value) {
            addCriterion("updated_at <>", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThan(LocalDateTime value) {
            addCriterion("updated_at >", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("updated_at >=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThan(LocalDateTime value) {
            addCriterion("updated_at <", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("updated_at <=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIn(List<LocalDateTime> values) {
            addCriterion("updated_at in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotIn(List<LocalDateTime> values) {
            addCriterion("updated_at not in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("updated_at between", value1, value2, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("updated_at not between", value1, value2, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedByIsNull() {
            addCriterion("updated_by is null");
            return (Criteria) this;
        }

        public Criteria andUpdatedByIsNotNull() {
            addCriterion("updated_by is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatedByEqualTo(Long value) {
            addCriterion("updated_by =", value, "updatedBy");
            return (Criteria) this;
        }

        public Criteria andUpdatedByNotEqualTo(Long value) {
            addCriterion("updated_by <>", value, "updatedBy");
            return (Criteria) this;
        }

        public Criteria andUpdatedByGreaterThan(Long value) {
            addCriterion("updated_by >", value, "updatedBy");
            return (Criteria) this;
        }

        public Criteria andUpdatedByGreaterThanOrEqualTo(Long value) {
            addCriterion("updated_by >=", value, "updatedBy");
            return (Criteria) this;
        }

        public Criteria andUpdatedByLessThan(Long value) {
            addCriterion("updated_by <", value, "updatedBy");
            return (Criteria) this;
        }

        public Criteria andUpdatedByLessThanOrEqualTo(Long value) {
            addCriterion("updated_by <=", value, "updatedBy");
            return (Criteria) this;
        }

        public Criteria andUpdatedByIn(List<Long> values) {
            addCriterion("updated_by in", values, "updatedBy");
            return (Criteria) this;
        }

        public Criteria andUpdatedByNotIn(List<Long> values) {
            addCriterion("updated_by not in", values, "updatedBy");
            return (Criteria) this;
        }

        public Criteria andUpdatedByBetween(Long value1, Long value2) {
            addCriterion("updated_by between", value1, value2, "updatedBy");
            return (Criteria) this;
        }

        public Criteria andUpdatedByNotBetween(Long value1, Long value2) {
            addCriterion("updated_by not between", value1, value2, "updatedBy");
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