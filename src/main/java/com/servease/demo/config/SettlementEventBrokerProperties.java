package com.servease.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "settlement.broker")
public class SettlementEventBrokerProperties {

    /**
     * Name of the exchange where settlement events are published.
     */
    private String exchange = "servease.order.settlement.exchange";

    /**
     * Routing key used to deliver settlement events to the queue.
     */
    private String routingKey = "servease.order.settlement";

    /**
     * Queue that consumers listen to for settlement events.
     */
    private String queue = "servease.order.settlement.queue";
}
