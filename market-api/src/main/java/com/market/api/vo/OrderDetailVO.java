package com.market.api.vo;

import com.market.common.domain.OrderDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends OrderDetail {

    // 商品名称
    private String productName;

}