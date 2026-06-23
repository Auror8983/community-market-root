package com.market.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.market.api.dto.CartItemDTO;
import com.market.api.dto.CreateOrderDTO;
import com.market.api.service.OrderService;
import com.market.api.service.ProductService;
import com.market.api.service.UserService;
import com.market.api.vo.*;
import com.market.common.domain.OrderDetail;
import com.market.common.domain.Orders;
import com.market.common.domain.SysUser;
import com.market.common.exception.BizException;
import com.market.order.mapper.OrderDetailMapper;
import com.market.order.mapper.OrderMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@DubboService
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @DubboReference
    private ProductService productService;
    @DubboReference
    private UserService userService;

    // --- 提取公共方法：计算是否打折 ---
    private boolean checkDiscount(String productId, String batchId) {
        var productVO = productService.getProductDetails(productId);
        var batch = productService.getBatch(batchId);

        if (productVO == null || batch == null) return false;

        // 【核心修改】读取商品配置的临期天数
        int threshold = (productVO.getNearExpiryDays() != null) ? productVO.getNearExpiryDays() : 30;

        LocalDate now = LocalDate.now();
        long days = batch.getExpirationDate().toEpochDay() - now.toEpochDay();

        // 未过期 且 在阈值内
        return days >= 0 && days <= threshold;
    }

    @Override
    public CartCalcVO previewCart(List<CartItemDTO> items) {
        CartCalcVO result = new CartCalcVO();
        List<CartItemVO> voList = new ArrayList<>();
        double total = 0.0;

        for (CartItemDTO item : items) {
            CartItemVO vo = new CartItemVO();
            vo.setProductId(item.getProductId());
            vo.setBatchId(item.getBatchId());
            vo.setQuantity(item.getQuantity());

            var productVO = productService.getProductDetails(item.getProductId());
            if (productVO != null) {
                vo.setProductName(productVO.getName());
                vo.setOriginalPrice(productVO.getPrice());

                // 使用动态阈值判断
                boolean isNearExpiry = checkDiscount(item.getProductId(), item.getBatchId());
                vo.setIsDiscounted(isNearExpiry);

                double price = productVO.getPrice().doubleValue();
                if (isNearExpiry) {
                    price = price * 0.8;
                }

                java.math.BigDecimal finalPriceBd = new java.math.BigDecimal(price).setScale(2, java.math.RoundingMode.HALF_UP);
                vo.setFinalPrice(finalPriceBd);

                double subtotal = price * item.getQuantity();
                java.math.BigDecimal subtotalBd = new java.math.BigDecimal(subtotal).setScale(2, java.math.RoundingMode.HALF_UP);
                vo.setSubtotal(subtotalBd);

                total += subtotal;
            }
            voList.add(vo);
        }

        result.setItems(voList);
        result.setTotalAmount(new java.math.BigDecimal(total).setScale(2, java.math.RoundingMode.HALF_UP));
        return result;
    }

    @Override
    @Transactional
    public String createOrder(CreateOrderDTO dto) {
        String finalName = dto.getReceiverName();
        String finalPhone = dto.getReceiverPhone();
        String finalAddress = dto.getReceiverAddress();

        if (finalName == null || finalAddress == null) {
            SysUser user = userService.getUserById(dto.getUserId());
            if (user != null) {
                if (finalName == null || finalName.isEmpty()) finalName = user.getRealName();
                if (finalPhone == null || finalPhone.isEmpty()) finalPhone = user.getPhone();
                if (finalAddress == null || finalAddress.isEmpty()) finalAddress = user.getAddress();
            }
        }

        if (finalAddress == null || finalAddress.isEmpty()) {
            throw new BizException("收货地址不能为空");
        }

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long count = orderMapper.countByDate(LocalDate.now().toString());
        String orderNo = dateStr + String.format("%04d", (count == null ? 0 : count) + 1);

        double totalAmount = 0.0;
        List<OrderDetail> details = new ArrayList<>();

        for (CreateOrderDTO.Item item : dto.getItems()) {
            productService.decreaseStock(item.getBatchId(), item.getQuantity());

            var productVO = productService.getProductDetails(item.getProductId());

            // 使用动态阈值判断
            boolean isNearExpiry = checkDiscount(item.getProductId(), item.getBatchId());

            double unitPrice = productVO.getPrice().doubleValue();
            if (isNearExpiry) unitPrice = unitPrice * 0.8;

            double subtotal = unitPrice * item.getQuantity();
            totalAmount += subtotal;

            OrderDetail od = new OrderDetail();
            od.setOrderNo(orderNo);
            od.setProductId(item.getProductId());
            od.setBatchId(item.getBatchId());
            od.setQuantity(item.getQuantity());
            od.setUnitPrice(new java.math.BigDecimal(unitPrice));
            od.setSubtotal(new java.math.BigDecimal(subtotal));
            details.add(od);
        }

        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUserId(dto.getUserId());
        order.setStatus("CREATED");
        order.setTotalAmount(new java.math.BigDecimal(totalAmount));
        order.setReceiverName(finalName);
        order.setReceiverPhone(finalPhone);
        order.setReceiverAddress(finalAddress);
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);
        for (OrderDetail d : details) {
            orderDetailMapper.insert(d);
        }

        return orderNo;
    }

    @Override
    public void payOrder(String orderNo) {
        Orders order = orderMapper.selectById(orderNo);
        if(order != null && "CREATED".equals(order.getStatus())) {
            order.setStatus("PAID"); // 变为：待配送
            orderMapper.updateById(order);
        }
    }

    @Override
    public void deliverOrder(String orderNo) {
        Orders order = orderMapper.selectById(orderNo);
        if(order != null && "PAID".equals(order.getStatus())) {
            order.setStatus("DELIVERING"); // 变为：配送中
            order.setStartDeliveryTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }
    }

    @Override
    public void completeOrder(String orderNo) {
        Orders order = orderMapper.selectById(orderNo);
        if(order != null && "DELIVERING".equals(order.getStatus())) {
            order.setStatus("COMPLETED"); // 变为：已完成
            order.setFinishTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNo) {
        Orders order = orderMapper.selectById(orderNo);
        if(order == null) throw new BizException("订单不存在");
        if(!"CREATED".equals(order.getStatus())) {
            throw new BizException("当前状态无法取消");
        }

        // 1. 恢复库存
        List<OrderDetail> details = orderDetailMapper.selectList(new QueryWrapper<OrderDetail>().eq("order_no", orderNo));
        for (OrderDetail d : details) {
            var batch = productService.getBatch(d.getBatchId());
            if (batch != null) {
                productService.updateBatchStock(d.getBatchId(), batch.getStockQuantity() + d.getQuantity());
            }
        }

        // 2. 删除订单
        orderDetailMapper.delete(new QueryWrapper<OrderDetail>().eq("order_no", orderNo));
        orderMapper.deleteById(orderNo);
    }

    @Override
    public void updateReceiverInfo(String orderNo, String name, String phone, String address) {
        Orders order = orderMapper.selectById(orderNo);
        if(order == null) throw new BizException("订单不存在");
        if(!"CREATED".equals(order.getStatus())) {
            throw new BizException("订单已付款或发货，无法修改地址");
        }
        order.setReceiverName(name);
        order.setReceiverPhone(phone);
        order.setReceiverAddress(address);
        orderMapper.updateById(order);
    }

    @Override
    public List<OrderVO> getOrderList(String userId, String status) {
        QueryWrapper<Orders> query = new QueryWrapper<>();
        if (userId != null) query.eq("user_id", userId);
        if (status != null && !status.isEmpty()) query.eq("status", status);
        query.orderByDesc("create_time");

        List<Orders> orders = orderMapper.selectList(query);
        return orders.stream().map(o -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(o, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public OrderVO getOrderDetail(String orderNo) {
        Orders order = orderMapper.selectById(orderNo);
        if (order == null) return null;

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);

        List<OrderDetail> details = orderDetailMapper.selectList(new QueryWrapper<OrderDetail>().eq("order_no", orderNo));


        List<OrderDetailVO> detailVOs = details.stream().map(d -> {
            OrderDetailVO dvo = new OrderDetailVO();
            BeanUtils.copyProperties(d, dvo);
            var prod = productService.getProductDetails(d.getProductId());
            if(prod != null) dvo.setProductName(prod.getName());
            return dvo;
        }).collect(Collectors.toList());

        vo.setDetails(detailVOs);
        return vo;
    }

    @Override
    public List<Map<String, Object>> getSalesLeaderboard() {
        return orderDetailMapper.selectSalesLeaderboard();
    }
    @Override
    public List<ProductStatsVO> getProductStats(String type, BigDecimal minPrice, BigDecimal maxPrice) {
        return orderDetailMapper.selectProductStats(type, minPrice, maxPrice);
    }

    @Override
    public List<UserStatsVO> getUserStats() {
        return orderMapper.selectUserStats();
    }
}