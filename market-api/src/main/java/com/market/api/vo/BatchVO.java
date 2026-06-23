package com.market.api.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class BatchVO implements Serializable {
    private String batchId;
    private String productId;
    private Integer stockQuantity;
    private LocalDate productionDate;
    private LocalDate expirationDate;

    private Integer status; // 1上架 0下架

    @JsonProperty("isNearExpiry")
    @JsonAlias({"nearExpiry"})
    private boolean nearExpiry;

    @JsonProperty("isExpired")
    @JsonAlias({"expired"})
    private boolean expired;

    private String productName;
}