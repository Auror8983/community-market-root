package com.market.api.vo;

import com.market.common.domain.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Orders {

    // 订单包含的商品明细列表
    private List<OrderDetailVO> details;

}