package com.servease.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sales_daily",
        uniqueConstraints = @UniqueConstraint(name = "ux_sales_daily_store_date", columnNames = {"store_id", "sales_date"}),
        indexes = @Index(name = "ix_sales_daily_store_date", columnList = "store_id, sales_date"))
public class SalesDaily extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "sales_date", nullable = false)
    private LocalDate date;

    @Column(name = "daily_net_sales", nullable = false)
    @Builder.Default
    private Long dailyNetSales = 0L;

    @Column(name = "order_count", nullable = false)
    @Builder.Default
    private Integer orderCount = 0;

    @Column(name = "daily_avg_order_value", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal dailyAverageOrderValue = BigDecimal.ZERO;

    @Column(name = "daily_canceled_amount", nullable = false)
    @Builder.Default
    private Long dailyCanceledAmount = 0L;

    public void applyOrder(long dailyNetSalesDelta, int orderCountDelta) {
        // 증감값을 받아 누적 매출·주문 건수를 조정하면서 일 평균을 함께 갱신한다.
        this.dailyNetSales = this.dailyNetSales + dailyNetSalesDelta;
        this.orderCount = this.orderCount + orderCountDelta;

        if (orderCount == null || orderCount <= 0) {
            this.dailyAverageOrderValue = BigDecimal.ZERO;
            return;
        }

        this.dailyAverageOrderValue = BigDecimal.valueOf(this.dailyNetSales)
                .divide(BigDecimal.valueOf(this.orderCount), 2, RoundingMode.HALF_UP);
    }

    public static SalesDaily createDailyAggregate(Store store, LocalDate date, long dailyNetSales, int orderCount) {
        BigDecimal avg = orderCount > 0
                ? BigDecimal.valueOf(dailyNetSales).divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return SalesDaily.builder()
                .store(store)
                .date(date)
                .dailyNetSales(dailyNetSales)
                .orderCount(orderCount)
                .dailyCanceledAmount(0L)
                .dailyAverageOrderValue(avg)
                .build();
    }
}
