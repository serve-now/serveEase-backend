package com.servease.demo.messaging;

import com.servease.demo.service.SalesAggregationService;
import com.servease.demo.service.event.OrderFullyPaidEvent;
import com.servease.demo.service.event.OrderRefundedEvent;
import com.servease.demo.support.MariaDbContainerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest
class OrderSettlementMessagingIntegrationTest extends MariaDbContainerTestBase {

    private static final String EXCHANGE = "servease.order.settlement.integration.exchange";
    private static final String ROUTING_KEY = "servease.order.settlement.integration";
    private static final String QUEUE = "servease.order.settlement.integration.queue";

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
        registry.add("settlement.broker.exchange", () -> EXCHANGE);
        registry.add("settlement.broker.routing-key", () -> ROUTING_KEY);
        registry.add("settlement.broker.queue", () -> QUEUE);
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private RecordingSalesAggregationService recordingSalesAggregationService;

    @Test
    void orderFullyPaidEvent_isPublishedToRabbit_andConsumed() {
        Long orderId = 9001L;

        eventPublisher.publishEvent(new OrderFullyPaidEvent(orderId));

        recordingSalesAggregationService.awaitSettledOrder(orderId);
        assertThat(recordingSalesAggregationService.getSettledOrders()).contains(orderId);
    }

    @Test
    void orderRefundedEvent_isPublishedToRabbit_andConsumed() {
        Long orderId = 9002L;
        Long storeId = 77L;
        Integer refundAmount = 12_000;
        OffsetDateTime refundedAt = OffsetDateTime.now();

        eventPublisher.publishEvent(new OrderRefundedEvent(orderId, storeId, refundAmount, refundedAt));

        recordingSalesAggregationService.awaitRefund(storeId, refundAmount);
        assertThat(recordingSalesAggregationService.getRefunds())
                .anyMatch(record -> record.storeId().equals(storeId) && record.refundAmount().equals(refundAmount));
    }

    @TestConfiguration
    @EnableConfigurationProperties
    static class TestConfig {

        @Bean
        @Primary
        RecordingSalesAggregationService recordingSalesAggregationService() {
            return new RecordingSalesAggregationService();
        }
    }

    static class RecordingSalesAggregationService extends SalesAggregationService {

        private final Set<Long> settledOrders = ConcurrentHashMap.newKeySet();
        private final List<RefundRecord> refunds = new CopyOnWriteArrayList<>();

        RecordingSalesAggregationService() {
            super(null, null, null);
        }

        @Override
        public void upsertDaily(Long orderId) {
            settledOrders.add(orderId);
        }

        @Override
        public void recordRefund(Long storeId, OffsetDateTime refundedAt, Integer refundAmount) {
            refunds.add(new RefundRecord(storeId, refundAmount));
        }

        void awaitSettledOrder(Long orderId) {
            await().atMost(Duration.ofSeconds(5))
                    .until(() -> settledOrders.contains(orderId));
        }

        void awaitRefund(Long storeId, Integer refundAmount) {
            await().atMost(Duration.ofSeconds(5))
                    .until(() -> refunds.stream()
                            .anyMatch(record -> record.storeId().equals(storeId)
                                    && record.refundAmount().equals(refundAmount)));
        }

        Set<Long> getSettledOrders() {
            return settledOrders;
        }

        List<RefundRecord> getRefunds() {
            return refunds;
        }
    }

    record RefundRecord(Long storeId, Integer refundAmount) {
    }
}
