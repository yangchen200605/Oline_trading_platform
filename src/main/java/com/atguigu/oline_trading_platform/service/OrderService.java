package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.dto.OrdersSubmitDTO;
import com.atguigu.oline_trading_platform.vo.OrderSubmitVO;

public interface OrderService {

    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /** 简化支付：直接改为待接单 */
    void payment(Long orderId);
}
