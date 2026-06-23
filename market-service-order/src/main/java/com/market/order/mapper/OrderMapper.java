package com.market.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.market.api.vo.UserStatsVO;
import com.market.common.domain.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    @Select("SELECT COUNT(*) FROM orders WHERE create_time LIKE CONCAT(#{date}, '%')")
    Long countByDate(@Param("date") String date);


    @Select("SELECT " +
            "  o.user_id as userId, " +
            "  u.real_name as realName, " +
            "  u.username as username, " +
            "  u.phone as phone, " +
            "  COUNT(o.order_no) as orderCount, " +
            "  SUM(o.total_amount) as totalSpent " +
            "FROM orders o " +
            "LEFT JOIN sys_user u ON o.user_id = u.user_id " +
            "WHERE o.status IN ('PAID', 'DELIVERING', 'COMPLETED') " +
            "GROUP BY o.user_id, u.real_name, u.username, u.phone " +
            "ORDER BY totalSpent DESC")
    List<UserStatsVO> selectUserStats();
}