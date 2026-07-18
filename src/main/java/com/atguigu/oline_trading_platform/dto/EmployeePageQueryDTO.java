package com.atguigu.oline_trading_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePageQueryDTO implements Serializable {

    private int page;
    private int pageSize;
    private String name;
}
