package com.market.api.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProductStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private String type;
    private BigDecimal price;
    private String factoryName;
    private Integer totalSold;
    private BigDecimal totalRevenue;
}