package jp.co.highjunk.equipment.dao.mapper;

import jakarta.annotation.Generated;
import java.util.List;
import jp.co.highjunk.equipment.dao.entity.Loan;
import jp.co.highjunk.equipment.dao.entity.LoanExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoanMapper {
    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    long countByExample(LoanExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByExample(LoanExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int deleteByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insert(Loan row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int insertSelective(Loan row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    List<Loan> selectByExample(LoanExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    Loan selectByPrimaryKey(Long id);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExampleSelective(@Param("row") Loan row, @Param("example") LoanExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByExample(@Param("row") Loan row, @Param("example") LoanExample example);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKeySelective(Loan row);

    @Generated("org.mybatis.generator.api.MyBatisGenerator")
    int updateByPrimaryKey(Loan row);
}