package com.atguigu.oline_trading_platform.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "oline.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfiguration {

    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(RabbitMQConstant.ORDER_DELAY_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange orderCancelExchange() {
        return new DirectExchange(RabbitMQConstant.ORDER_CANCEL_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(RabbitMQConstant.ORDER_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstant.ORDER_CANCEL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstant.ORDER_CANCEL_ROUTING_KEY)
                .withArgument("x-message-ttl", RabbitMQConstant.ORDER_TIMEOUT_MILLIS)
                .build();
    }

    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(RabbitMQConstant.ORDER_CANCEL_QUEUE).build();
    }

    @Bean
    public Binding orderDelayBinding(Queue orderDelayQueue, DirectExchange orderDelayExchange) {
        return BindingBuilder.bind(orderDelayQueue)
                .to(orderDelayExchange)
                .with(RabbitMQConstant.ORDER_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding orderCancelBinding(Queue orderCancelQueue, DirectExchange orderCancelExchange) {
        return BindingBuilder.bind(orderCancelQueue)
                .to(orderCancelExchange)
                .with(RabbitMQConstant.ORDER_CANCEL_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
