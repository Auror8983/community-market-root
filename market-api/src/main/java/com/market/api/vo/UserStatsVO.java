package com.market.api.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UserStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String realName;
    private String phone;
    private Integer orderCount;
    private BigDecimal totalSpent;
}