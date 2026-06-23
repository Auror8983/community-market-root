package com.market.product.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.market.common.domain.ProductBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductBatchMapper extends BaseMapper<ProductBatch> {
    @Select("SELECT MAX(batch_id) FROM product_batch WHERE product_id = #{productId}")
    String getMaxBatchIdByProductId(String productId);
}