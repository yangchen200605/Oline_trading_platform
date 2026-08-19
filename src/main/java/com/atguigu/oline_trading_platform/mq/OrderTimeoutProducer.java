package com.atguigu.oline_trading_platform.mq;

import com.atguigu.oline_trading_platform.config.RabbitMQConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutProducer {

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;

    public void sendDelayCancel(Long orderId) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            return;
        }
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.ORDER_DELAY_EXCHANGE,
                    RabbitMQConstant.ORDER_DELAY_ROUTING_KEY,
                    String.valueOf(orderId));
            log.info("已发送订单超时取消延迟消息, orderId={}", orderId);
        } catch (Exception e) {
            log.warn("发送订单超时取消消息失败，将由定时任务兜底, orderId={}: {}", orderId, e.getMessage());
        }
    }
}
