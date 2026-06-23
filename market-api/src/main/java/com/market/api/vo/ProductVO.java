package com.market.api.vo;

import com.market.common.domain.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductVO extends Product {
    // 批次信息
    private List<BatchVO> batches;
    // 总库存
    private Integer totalStock;
    // 厂家名称
    private String factoryName;
    private Integer buyCount;
    private String selectedBatchId;
}