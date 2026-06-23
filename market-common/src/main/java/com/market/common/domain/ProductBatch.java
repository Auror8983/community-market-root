package com.market.common.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@TableName("product_batch")
public class ProductBatch implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private String batchId;
    private String productId;
    private Integer stockQuantity;
    private LocalDate productionDate;
    private LocalDate expirationDate;
    private Integer status;
}