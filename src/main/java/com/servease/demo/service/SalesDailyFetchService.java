package com.servease.demo.service;

import com.servease.demo.model.entity.SalesDaily;
import com.servease.demo.repository.SalesDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesDailyFetchService {

    private final SalesDailyRepository salesDailyRepository;

    public List<SalesDaily> findByStoreAndDateRange(Long storeId, LocalDate from, LocalDate to) {
        return salesDailyRepository.findByStoreIdAndDateBetweenOrderByDateAsc(storeId, from, to);
    }

    public List<SalesAggregationResult> findWeeklyAggregates(Long storeId, LocalDate from, LocalDate to) {
        return salesDailyRepository.findWeeklyAggregates(storeId, from, to).stream()
                .map(this::mapWeeklyAggregateRow)
                .collect(Collectors.toList());
    }

    public List<SalesAggregationResult> findMonthlyAggregates(Long storeId, LocalDate from, LocalDate to) {
        return salesDailyRepository.findMonthlyAggregates(storeId, from, to).stream()
                .map(this::mapMonthlyAggregateRow)
                .collect(Collectors.toList());
    }

    private SalesAggregationResult mapWeeklyAggregateRow(Object[] row) {
        LocalDate periodStart = extractDate(row[0]);
        Integer isoWeek = row[1] != null ? ((Number) row[1]).intValue() : null;
        long netSales = extractLong(row[2]);
        int orderCount = extractInt(row[3]);
        long canceledAmount = extractLong(row[4]);
        return new SalesAggregationResult(periodStart, isoWeek, netSales, orderCount, canceledAmount);
    }

    private SalesAggregationResult mapMonthlyAggregateRow(Object[] row) {
        LocalDate periodStart = extractDate(row[0]);
        long netSales = extractLong(row[1]);
        int orderCount = extractInt(row[2]);
        long canceledAmount = extractLong(row[3]);
        return new SalesAggregationResult(periodStart, null, netSales, orderCount, canceledAmount);
    }

    private LocalDate extractDate(Object value) {
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof String str) {
            return LocalDate.parse(str);
        }
        throw new IllegalArgumentException("Unsupported date value: " + value);
    }

    private long extractLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private int extractInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    public record SalesAggregationResult(LocalDate periodStart,
                                         Integer isoWeek,
                                         long netSales,
                                         int orderCount,
                                         long canceledAmount) {
    }
}
