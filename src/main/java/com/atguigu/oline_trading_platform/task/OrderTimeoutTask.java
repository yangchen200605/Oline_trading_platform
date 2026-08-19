package com.atguigu.oline_trading_platform.task;

import com.atguigu.oline_trading_platform.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderService orderService;

    /** 每分钟扫描一次超时未支付订单，作为 MQ 的兜底 */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        orderService.processTimeoutOrders();
    }
}
