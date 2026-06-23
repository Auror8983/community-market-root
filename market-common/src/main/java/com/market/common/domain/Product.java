package com.market.common.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("product")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private String productId;
    private String name;
    private BigDecimal price;
    private String type;

    // 保质期数值
    private Integer shelfLifeValue;
    // 保质期单位
    private String shelfLifeUnit;

    // 库存预警阈值
    private Integer stockAlertThreshold;

    // 厂家ID
    private String factoryId;

    // 状态 (1:上架, 0:下架)
    private Integer status;

    //临期界限天数 (自定义打折触发时间)
    private Integer nearExpiryDays;
}