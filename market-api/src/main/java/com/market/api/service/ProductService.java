package com.market.api.service;

import com.market.api.dto.ProductInboundDTO;
import com.market.api.vo.ProductVO;
import com.market.common.domain.Product;
import com.market.common.domain.ProductBatch;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductService {

    List<ProductVO> searchProducts(String name, String type,
                                   BigDecimal minPrice, BigDecimal maxPrice,
                                   Integer minStock, Integer maxStock, // 必须有这就两个
                                   boolean onlyOnShelf);
    // 获取单个商品详情
    ProductVO getProductDetails(String productId);

    // 获取指定批次
    ProductBatch getBatch(String batchId);

    // 扣减库存
    void decreaseStock(String batchId, int quantity);

    // 员工入库
    void inboundProduct(ProductInboundDTO dto);

    // 管理员添加/修改商品基础信息
    void saveOrUpdateProduct(Product product);

    // 获取预警商品 (库存不足 + 临期)
    Map<String, List<?>> getWarningData(); // key: "lowStock", "nearExpiry", "expired"

    // 清除过期商品
    void clearExpiredProducts();

    // 自动下架检查 (定时任务调用)
    void checkAndOffShelf();

    // 更新商品状态 (上架/下架)
    void updateProductStatus(String productId, Integer status);

    // 直接修改某批次的库存 (用于库存盘点/修正)
    void updateBatchStock(String batchId, Integer newQuantity);
    // 修改指定批次的状态
    void updateBatchStatus(String batchId, Integer status);
    // 删除指定批次
    void deleteBatch(String batchId);
}