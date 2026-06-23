package com.market.web.controller;

import com.market.api.dto.ProductInboundDTO;
import com.market.api.service.ProductService;
import com.market.api.vo.ProductVO;
import com.market.common.domain.Product;
import com.market.common.result.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@CrossOrigin
public class ProductController {

    @DubboReference
    private ProductService productService;

    /**
     * 商品列表 (支持筛选: 名称, 类型, 价格区间, 库存区间)
     */
    @GetMapping("/list")
    public Result<List<ProductVO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minStock, // 新增参数
            @RequestParam(required = false) Integer maxStock, // 新增参数
            @RequestParam(defaultValue = "false") boolean onlyOnShelf) {

        return Result.success(productService.searchProducts(
                name, type, minPrice, maxPrice, minStock, maxStock, onlyOnShelf
        ));
    }

    // 商品详情
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable String id) {
        return Result.success(productService.getProductDetails(id));
    }

    // --- 员工/管理员操作 ---

    // 添加入库
    @PostMapping("/inbound")
    public Result<Void> inbound(@RequestBody ProductInboundDTO dto) {
        productService.inboundProduct(dto);
        return Result.success();
    }

    // 获取预警信息 (库存不足、临期、过期)
    @GetMapping("/warning")
    public Result<Map<String, List<?>>> getWarning() {
        return Result.success(productService.getWarningData());
    }

    // 清除过期商品
    @PostMapping("/clear-expired")
    public Result<Void> clearExpired() {
        productService.clearExpiredProducts();
        return Result.success();
    }

    // 新增/修改商品基础信息
    @PostMapping("/save")
    public Result<Void> save(@RequestBody Product product) {
        productService.saveOrUpdateProduct(product);
        return Result.success();
    }
    // 修改商品总状态 (上架/下架)
    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        String productId = (String) params.get("productId");
        Integer status = (Integer) params.get("status");
        productService.updateProductStatus(productId, status);
        return Result.success();
    }
    // 修改批次库存
    @PostMapping("/batch/update")
    public Result<Void> updateBatchStock(@RequestParam String batchId, @RequestParam Integer quantity) {
        productService.updateBatchStock(batchId, quantity);
        return Result.success();
    }
    // 修改批次状态
    @PostMapping("/batch/status")
    public Result<Void> updateBatchStatus(@RequestBody Map<String, Object> params) {
        String batchId = (String) params.get("batchId");
        Integer status = (Integer) params.get("status");
        productService.updateBatchStatus(batchId, status);
        return Result.success();
    }
    // 删除批次
    @DeleteMapping("/batch/{batchId}")
    public Result<Void> deleteBatch(@PathVariable String batchId) {
        productService.deleteBatch(batchId);
        return Result.success();
    }
}