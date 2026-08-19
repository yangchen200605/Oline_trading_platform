package com.atguigu.oline_trading_platform.config;

public final class RabbitMQConstant {

    public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";

    public static final String ORDER_CANCEL_EXCHANGE = "order.cancel.exchange";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    /** 未支付超时时间（毫秒），默认 15 分钟 */
    public static final int ORDER_TIMEOUT_MILLIS = 15 * 60 * 1000;

    private RabbitMQConstant() {
    }
}
