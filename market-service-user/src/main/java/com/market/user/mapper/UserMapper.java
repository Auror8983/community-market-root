package com.market.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.market.common.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {

    @Select("SELECT MAX(CAST(user_id AS UNSIGNED)) FROM sys_user")
    String selectMaxUserId();
}