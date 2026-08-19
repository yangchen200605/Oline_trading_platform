package com.atguigu.oline_trading_platform.vo;

import com.atguigu.oline_trading_platform.entity.OrderDetail;
import com.atguigu.oline_trading_platform.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Orders {

    private List<OrderDetail> orderDetailList;
}
