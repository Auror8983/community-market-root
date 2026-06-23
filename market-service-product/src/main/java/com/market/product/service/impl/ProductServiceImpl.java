package com.market.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.market.api.dto.ProductInboundDTO;
import com.market.api.service.ProductService;
import com.market.api.vo.BatchVO;
import com.market.api.vo.ProductVO;
import com.market.common.domain.Product;
import com.market.common.domain.ProductBatch;
import com.market.common.exception.BizException;
import com.market.product.mapper.ProductBatchMapper;
import com.market.product.mapper.ProductMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@DubboService
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductBatchMapper batchMapper;

    @Override
    public List<ProductVO> searchProducts(String name, String type,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          Integer minStock, Integer maxStock,
                                          boolean onlyOnShelf) {
        QueryWrapper<Product> query = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) query.like("name", name);
        if (type != null && !type.isEmpty()) query.eq("type", type);
        if (minPrice != null) query.ge("price", minPrice);
        if (maxPrice != null) query.le("price", maxPrice);
        if (onlyOnShelf) query.eq("status", 1);

        List<Product> products = productMapper.selectList(query);

        List<ProductVO> voList = products.stream()
                .map(p -> convertToVO(p, onlyOnShelf))
                .collect(Collectors.toList());

        if (minStock != null || maxStock != null) {
            voList = voList.stream().filter(vo -> {
                int totalStock = vo.getTotalStock();
                boolean matchMin = (minStock == null || totalStock >= minStock);
                boolean matchMax = (maxStock == null || totalStock <= maxStock);
                return matchMin && matchMax;
            }).collect(Collectors.toList());
        }
        return voList;
    }

    @Override
    public ProductVO getProductDetails(String productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) return null;
        return convertToVO(product, false);
    }

    private ProductVO convertToVO(Product product, boolean filterOffShelfBatches) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);

        if (product.getFactoryId() != null) {
            String factoryName = productMapper.selectFactoryNameById(product.getFactoryId());
            vo.setFactoryName(factoryName);
        }

        // 获取该商品的临期阈值，默认为30天
        int thresholdDays = (product.getNearExpiryDays() != null) ? product.getNearExpiryDays() : 30;

        QueryWrapper<ProductBatch> batchQuery = new QueryWrapper<>();
        batchQuery.eq("product_id", product.getProductId());
        batchQuery.orderByAsc("expiration_date");

        if (filterOffShelfBatches) {
            batchQuery.eq("status", 1);
            batchQuery.gt("stock_quantity", 0);
        }

        List<ProductBatch> batches = batchMapper.selectList(batchQuery);
        LocalDate now = LocalDate.now(); // 【核心】基于服务器实时时间

        List<BatchVO> batchVOs = batches.stream().map(b -> {
            BatchVO bvo = new BatchVO();
            BeanUtils.copyProperties(b, bvo);

            boolean isExpired = b.getExpirationDate().isBefore(now);

            long daysUntilExpiration = b.getExpirationDate().toEpochDay() - now.toEpochDay();
            boolean isNearExpiry = !isExpired && daysUntilExpiration >= 0 && daysUntilExpiration <= thresholdDays;

            bvo.setExpired(isExpired);
            bvo.setNearExpiry(isNearExpiry);

            if (bvo.getStatus() == null) bvo.setStatus(1);

            return bvo;
        }).collect(Collectors.toList());

        vo.setBatches(batchVOs);
        int totalStock = batches.stream().mapToInt(ProductBatch::getStockQuantity).sum();
        vo.setTotalStock(totalStock);

        return vo;
    }

    @Override
    public ProductBatch getBatch(String batchId) { return batchMapper.selectById(batchId); }

    @Override
    @Transactional
    public void decreaseStock(String batchId, int quantity) {
        ProductBatch batch = batchMapper.selectById(batchId);
        if (batch == null) throw new BizException("批次不存在: " + batchId);
        if (batch.getStatus() != null && batch.getStatus() == 0) throw new BizException("该批次已下架");
        if (batch.getStockQuantity() < quantity) throw new BizException("库存不足");
        batch.setStockQuantity(batch.getStockQuantity() - quantity);
        batchMapper.updateById(batch);
    }

    @Override
    @Transactional
    public void inboundProduct(ProductInboundDTO dto) {
        Product product = productMapper.selectOne(new QueryWrapper<Product>().eq("name", dto.getProductName()));
        if (product == null) throw new BizException("商品不存在，请先添加商品基础信息");

        LocalDate expirationDate;
        if ("YEAR".equals(product.getShelfLifeUnit())) expirationDate = dto.getProductionDate().plusYears(product.getShelfLifeValue());
        else if ("MONTH".equals(product.getShelfLifeUnit())) expirationDate = dto.getProductionDate().plusMonths(product.getShelfLifeValue());
        else expirationDate = dto.getProductionDate().plusDays(product.getShelfLifeValue());

        String maxBatchId = batchMapper.getMaxBatchIdByProductId(product.getProductId());
        String newBatchId = (maxBatchId == null) ? product.getProductId() + "0001" :
                product.getProductId() + String.format("%04d", Long.parseLong(maxBatchId.substring(4)) + 1);

        ProductBatch batch = new ProductBatch();
        batch.setBatchId(newBatchId);
        batch.setProductId(product.getProductId());
        batch.setStockQuantity(dto.getQuantity());
        batch.setProductionDate(dto.getProductionDate());
        batch.setExpirationDate(expirationDate);
        batch.setStatus(1);
        batchMapper.insert(batch);
    }

    @Override
    public void saveOrUpdateProduct(Product product) {
        if (product.getProductId() != null && !product.getProductId().isEmpty()) {
            Product existing = productMapper.selectById(product.getProductId());
            if(existing != null) {
                if(product.getNearExpiryDays() == null) product.setNearExpiryDays(30);
                productMapper.updateById(product);
                return;
            }
        }

        if(product.getProductId() == null || product.getProductId().isEmpty()) {
            String maxIdStr = productMapper.selectMaxProductId();
            long nextId = 1;
            if (maxIdStr != null) {
                try { nextId = Long.parseLong(maxIdStr) + 1; } catch (NumberFormatException e) { nextId = 1000; }
            }
            product.setProductId(String.format("%04d", nextId));
        }

        // 默认临期天数
        if(product.getNearExpiryDays() == null) product.setNearExpiryDays(30);

        productMapper.insert(product);
    }

    @Override
    public void updateProductStatus(String productId, Integer status) {
        Product product = productMapper.selectById(productId);
        if(product != null) { product.setStatus(status); productMapper.updateById(product); }
    }
    @Override
    public void updateBatchStock(String batchId, Integer newQuantity) {
        ProductBatch batch = batchMapper.selectById(batchId);
        if(batch != null) { batch.setStockQuantity(newQuantity); batchMapper.updateById(batch); }
    }
    @Override
    public void updateBatchStatus(String batchId, Integer status) {
        ProductBatch batch = batchMapper.selectById(batchId);
        if(batch != null) { batch.setStatus(status); batchMapper.updateById(batch); }
    }
    @Override
    public void deleteBatch(String batchId) { batchMapper.deleteById(batchId); }


    @Override
    public Map<String, List<?>> getWarningData() {
        Map<String, List<?>> result = new HashMap<>();
        LocalDate now = LocalDate.now();

        // 1. 库存预警
        List<Product> allProducts = productMapper.selectList(null);
        List<ProductVO> lowStockList = allProducts.stream()
                .map(p -> convertToVO(p, false))
                .filter(vo -> vo.getTotalStock() < vo.getStockAlertThreshold())
                .collect(Collectors.toList());
        result.put("lowStock", lowStockList);

        // 2. 临期预警 (需要动态判断每个商品的阈值)
        // 先查出所有未过期的批次
        List<ProductBatch> activeBatches = batchMapper.selectList(new QueryWrapper<ProductBatch>()
                .gt("expiration_date", now)
                .gt("stock_quantity", 0));

        // 内存过滤：根据每个批次对应商品的 nearExpiryDays 判断
        List<BatchVO> nearExpiryVOs = activeBatches.stream().filter(batch -> {
            Product p = productMapper.selectById(batch.getProductId());
            if (p == null) return false;

            int threshold = (p.getNearExpiryDays() != null) ? p.getNearExpiryDays() : 30;
            long days = batch.getExpirationDate().toEpochDay() - now.toEpochDay();

            return days >= 0 && days <= threshold;
        }).map(b -> {
            BatchVO vo = new BatchVO();
            BeanUtils.copyProperties(b, vo);
            Product p = productMapper.selectById(b.getProductId());
            if(p != null) vo.setProductName(p.getName());
            return vo;
        }).collect(Collectors.toList());

        result.put("nearExpiry", nearExpiryVOs);

        // 3. 已过期
        List<ProductBatch> expired = batchMapper.selectList(new QueryWrapper<ProductBatch>()
                .le("expiration_date", now)
                .gt("stock_quantity", 0));
        List<BatchVO> expiredVOs = expired.stream().map(b -> {
            BatchVO vo = new BatchVO();
            BeanUtils.copyProperties(b, vo);
            Product p = productMapper.selectById(b.getProductId());
            if(p != null) vo.setProductName(p.getName());
            return vo;
        }).collect(Collectors.toList());

        result.put("expired", expiredVOs);

        return result;
    }

    @Override
    public void clearExpiredProducts() {
        LocalDate now = LocalDate.now();
        ProductBatch update = new ProductBatch();
        update.setStockQuantity(0);
        batchMapper.update(update, new QueryWrapper<ProductBatch>().lt("expiration_date", now));
    }

    @Override
    public void checkAndOffShelf() {}
}