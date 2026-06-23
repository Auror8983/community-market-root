package com.market.common.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("factory")
public class Factory implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private String factoryId;
    private String factoryName;
    private String contactPerson;
    private String phone;
    private String address;
}