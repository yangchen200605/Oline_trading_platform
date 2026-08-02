package com.atguigu.oline_trading_platform.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ShoppingCartDTO implements Serializable {

    private Long dishId;
    private String dishFlavor;
    private Integer number;
}
