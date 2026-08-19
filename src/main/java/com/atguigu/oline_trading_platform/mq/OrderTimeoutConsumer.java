package com.atguigu.oline_trading_platform.mq;

import com.atguigu.oline_trading_platform.config.RabbitMQConstant;
import com.atguigu.oline_trading_platform.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oline.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class OrderTimeoutConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConstant.ORDER_CANCEL_QUEUE)
    public void onMessage(String orderId) {
        log.info("收到订单超时取消消息, orderId={}", orderId);
        orderService.cancelIfUnpaid(Long.valueOf(orderId));
    }
}
