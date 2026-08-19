package com.atguigu.oline_trading_platform.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDataVO implements Serializable {

    private BigDecimal turnover;
    private Integer validOrderCount;
    private Double orderCompletionRate;
    private BigDecimal unitPrice;
    private Integer newUsers;
}
