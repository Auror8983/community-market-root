package com.market.api.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CartItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private String batchId;
    private Integer quantity;
    private BigDecimal originalPrice;
    private BigDecimal finalPrice;
    private BigDecimal subtotal;
    private Boolean isDiscounted;
}