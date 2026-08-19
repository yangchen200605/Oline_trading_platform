package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.OrdersCancelDTO;
import com.atguigu.oline_trading_platform.dto.OrdersPageQueryDTO;
import com.atguigu.oline_trading_platform.dto.OrdersRejectionDTO;
import com.atguigu.oline_trading_platform.dto.OrdersSubmitDTO;
import com.atguigu.oline_trading_platform.vo.OrderStatisticsVO;
import com.atguigu.oline_trading_platform.vo.OrderSubmitVO;
import com.atguigu.oline_trading_platform.vo.OrderVO;

public interface OrderService {

    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /** 简化支付：直接改为待接单 */
    void payment(Long orderId);

    void reminder(Long orderId);

    PageResult page(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO details(Long id);

    OrderVO userDetails(Long id);

    void userCancel(Long id);

    void repetition(Long id);

    OrderStatisticsVO statistics();

    void confirm(Long id);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    void adminCancel(OrdersCancelDTO ordersCancelDTO);

    void delivery(Long id);

    void complete(Long id);

    /** 超时未支付取消（定时任务 / MQ 共用） */
    void cancelIfUnpaid(Long orderId);

    void processTimeoutOrders();
}
