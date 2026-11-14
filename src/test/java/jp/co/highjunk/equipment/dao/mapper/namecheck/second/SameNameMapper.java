package jp.co.highjunk.equipment.dao.mapper.namecheck.second;

import org.apache.ibatis.annotations.Mapper;

/**
 * テストだけで使う mapper。名前（SameNameMapper）を、namecheck.first と namecheck.second でわざと同じにして、
 * 部品の名前がぶつからないこと（common.config.MyBatisConfig）を確かめる（MyBatisConfigTest）。
 */
@Mapper
public interface SameNameMapper {

	/** どちらの mapper が呼ばれたかが分かるように、決まった数（first は 1、second は 2）を返す。 */
	int selectNumber();
}
