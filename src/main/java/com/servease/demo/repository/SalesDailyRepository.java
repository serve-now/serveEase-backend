package com.servease.demo.repository;

import com.servease.demo.model.entity.SalesDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesDailyRepository extends JpaRepository<SalesDaily, Long> {
    Optional<SalesDaily> findByStoreIdAndDate(Long storeId, LocalDate date);

    List<SalesDaily> findByStoreIdAndDateBetweenOrderByDateAsc(Long storeId, LocalDate start, LocalDate end);

    @Query(value = """
            SELECT
                MIN(sales_date) AS period_start,
                WEEK(sales_date, 3) AS iso_week,
                SUM(daily_net_sales) AS net_sales,
                SUM(order_count) AS order_count,
                SUM(daily_canceled_amount) AS canceled_amount
            FROM sales_daily
            WHERE store_id = :storeId AND sales_date BETWEEN :start AND :end
            GROUP BY YEAR(sales_date), WEEK(sales_date, 3)
            ORDER BY period_start
            """, nativeQuery = true)
    List<Object[]> findWeeklyAggregates(@Param("storeId") Long storeId,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);

    @Query(value = """
            SELECT
                DATE_SUB(sales_date, INTERVAL DAYOFMONTH(sales_date) - 1 DAY) AS period_start,
                SUM(daily_net_sales) AS net_sales,
                SUM(order_count) AS order_count,
                SUM(daily_canceled_amount) AS canceled_amount
            FROM sales_daily
            WHERE store_id = :storeId AND sales_date BETWEEN :start AND :end
            GROUP BY YEAR(sales_date), MONTH(sales_date)
            ORDER BY period_start
            """, nativeQuery = true)
    List<Object[]> findMonthlyAggregates(@Param("storeId") Long storeId,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);
}
