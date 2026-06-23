package com.market.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.market.common.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    // 之前可能存在的查询低库存的方法
    @Select("SELECT * FROM product WHERE total_stock < stock_alert_threshold")
    List<Product> selectLowStockProducts();

    // 查询厂家名称
    @Select("SELECT factory_name FROM factory WHERE factory_id = #{factoryId}")
    String selectFactoryNameById(String factoryId);

    // 查询当前最大的数字型 ID ---
    @Select("SELECT MAX(CAST(product_id AS UNSIGNED)) FROM product")
    String selectMaxProductId();
}