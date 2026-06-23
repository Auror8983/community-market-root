package com.market.api.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class CreateOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    // 订单商品列表
    private List<Item> items;

    @Data
    public static class Item implements Serializable {

        private static final long serialVersionUID = 1L;

        private String productId;
        private String batchId;
        private Integer quantity;
    }
}