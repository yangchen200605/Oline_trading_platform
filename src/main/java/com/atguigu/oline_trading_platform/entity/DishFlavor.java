package com.atguigu.oline_trading_platform.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishFlavor implements Serializable {

    private Long id;
    private Long dishId;
    /** 口味名称，如辣度、忌口 */
    private String name;
    /** 口味选项 JSON，如 ["微辣","中辣","重辣"] */
    private String value;
}
