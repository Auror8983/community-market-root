package com.market.api.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class CartItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private String batchId;
    private Integer quantity;
}