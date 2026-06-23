package com.market.api.dto;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ProductInboundDTO implements Serializable {
    private String productName; // 根据名称自动匹配
    private Integer quantity;
    private LocalDate productionDate;
}