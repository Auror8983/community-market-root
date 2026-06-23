package com.market.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.market.common.domain.Factory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FactoryMapper extends BaseMapper<Factory> {

    // 查询当前最大的数字型 ID
    @Select("SELECT MAX(CAST(factory_id AS UNSIGNED)) FROM factory")
    String selectMaxFactoryId();
}