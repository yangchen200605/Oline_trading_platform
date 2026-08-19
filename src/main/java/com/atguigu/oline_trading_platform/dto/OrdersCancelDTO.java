package com.atguigu.oline_trading_platform.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersCancelDTO implements Serializable {

    private Long id;
    private String cancelReason;
}
