package com.market.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.market.api.vo.ProductStatsVO;
import com.market.common.domain.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    @Select("SELECT product_id, SUM(quantity) as total FROM order_detail GROUP BY product_id ORDER BY total DESC LIMIT 10")
    List<Map<String, Object>> selectSalesLeaderboard();


    @Select("<script>" +
            "SELECT " +
            "  od.product_id as productId, " +
            "  p.name as productName, " +
            "  p.type as type, " +
            "  p.price as price, " +
            "  SUM(od.quantity) as totalSold, " +
            "  SUM(od.subtotal) as totalRevenue " +
            "FROM order_detail od " +
            "LEFT JOIN product p ON od.product_id = p.product_id " +
            "LEFT JOIN orders o ON od.order_no = o.order_no " +
            "WHERE o.status IN ('PAID', 'DELIVERING', 'COMPLETED') " +
            "<if test='type != null and type != \"\"'> AND p.type = #{type} </if>" +
            "<if test='minPrice != null'> AND p.price &gt;= #{minPrice} </if>" +
            "<if test='maxPrice != null'> AND p.price &lt;= #{maxPrice} </if>" +
            "GROUP BY od.product_id, p.name, p.type, p.price " +
            "ORDER BY totalSold DESC " + // 默认销量倒序
            "</script>")
    List<ProductStatsVO> selectProductStats(@Param("type") String type,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice);
}