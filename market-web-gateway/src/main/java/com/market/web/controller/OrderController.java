package com.market.web.controller;

import com.market.api.dto.CartItemDTO;
import com.market.api.dto.CreateOrderDTO;
import com.market.api.service.OrderService;
import com.market.api.vo.CartCalcVO;
import com.market.api.vo.OrderVO;
import com.market.api.vo.ProductStatsVO;
import com.market.api.vo.UserStatsVO;
import com.market.common.result.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
public class OrderController {

    @DubboReference
    private OrderService orderService;

    // 创建订单
    @PostMapping("/create")
    public Result<String> create(@RequestBody CreateOrderDTO dto) {
        String orderNo = orderService.createOrder(dto);
        return Result.success(orderNo);
    }

    // 获取订单列表
    @GetMapping("/list")
    public Result<List<OrderVO>> list(@RequestParam(required = false) String userId,
                                      @RequestParam(required = false) String status) {
        return Result.success(orderService.getOrderList(userId, status));
    }

    // 获取订单详情
    @GetMapping("/detail/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderDetail(orderNo));
    }

    // 支付订单
    @PostMapping("/pay/{orderNo}")
    public Result<Void> pay(@PathVariable String orderNo) {
        orderService.payOrder(orderNo);
        return Result.success();
    }

    // 取消订单
    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo);
        return Result.success();
    }

    // 修改收货信息
    @PostMapping("/update-address")
    public Result<Void> updateAddress(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        String name = params.get("name");
        String phone = params.get("phone");
        String address = params.get("address");
        orderService.updateReceiverInfo(orderNo, name, phone, address);
        return Result.success();
    }

    // 发货 (管理员/员工操作)
    @PostMapping("/deliver/{orderNo}")
    public Result<Void> deliver(@PathVariable String orderNo) {
        orderService.deliverOrder(orderNo);
        return Result.success();
    }

    // 完成订单 (管理员/员工操作)
    @PostMapping("/complete/{orderNo}")
    public Result<Void> complete(@PathVariable String orderNo) {
        orderService.completeOrder(orderNo);
        return Result.success();
    }

    // 购物车预览
    @PostMapping("/cart/preview")
    public Result<CartCalcVO> previewCart(@RequestBody List<CartItemDTO> items) {
        return Result.success(orderService.previewCart(items));
    }
    // 商品销售统计接口
    @GetMapping("/stats/product")
    public Result<List<ProductStatsVO>> productStats(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return Result.success(orderService.getProductStats(type, minPrice, maxPrice));
    }

    // 用户消费统计接口
    @GetMapping("/stats/user")
    public Result<List<UserStatsVO>> userStats() {
        return Result.success(orderService.getUserStats());
    }
}