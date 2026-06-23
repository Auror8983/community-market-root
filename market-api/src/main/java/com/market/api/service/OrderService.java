package com.market.api.service;

import com.market.api.dto.CartItemDTO;
import com.market.api.dto.CreateOrderDTO;
import com.market.api.vo.CartCalcVO;
import com.market.api.vo.OrderVO;
import com.market.api.vo.ProductStatsVO;
import com.market.api.vo.UserStatsVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService {
    // 创建订单
    String createOrder(CreateOrderDTO dto);

    // 支付订单
    void payOrder(String orderNo);

    // 发货
    void deliverOrder(String orderNo);

    // 完成订单
    void completeOrder(String orderNo);

    // 取消订单
    void cancelOrder(String orderNo);

    // 修改收货信息
    void updateReceiverInfo(String orderNo, String name, String phone, String address);

    // 获取订单列表
    List<OrderVO> getOrderList(String userId, String status);

    // 获取订单详情
    OrderVO getOrderDetail(String orderNo);

    // 销售排行
    List<Map<String, Object>> getSalesLeaderboard();

    // 购物车预览计算
    CartCalcVO previewCart(List<CartItemDTO> items);

    //获取商品销售统计
    List<ProductStatsVO> getProductStats(String type, BigDecimal minPrice, BigDecimal maxPrice);

    // 获取用户消费统计
    List<UserStatsVO> getUserStats();
}