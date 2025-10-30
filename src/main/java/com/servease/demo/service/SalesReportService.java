package com.servease.demo.service;

import com.servease.demo.dto.response.SalesReportResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.SalesDaily;
import com.servease.demo.model.entity.Store;
import com.servease.demo.model.enums.PeriodType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesReportService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final SalesDailyFetchService salesDailyFetchService;
    private final StoreService storeService;

    public SalesReportResponse getSalesReport(Long storeId,
                                              LocalDate from,
                                              LocalDate to,
                                              PeriodType periodType) {
        validateInputs(storeId, from, to, periodType);

        Store store = storeService.getStore(storeId);

        List<SalesDaily> dailyRecords = salesDailyFetchService.findByStoreAndDateRange(store.getId(), from, to);

        long totalNetSales = dailyRecords.stream()
                .mapToLong(SalesDaily::getDailyNetSales)
                .sum();
        int totalOrders = dailyRecords.stream()
                .mapToInt(SalesDaily::getOrderCount)
                .sum();
        long totalCanceledAmount = dailyRecords.stream()
                .mapToLong(SalesDaily::getDailyCanceledAmount)
                .sum();

        BigDecimal totalAvgOrderValue = calculateAverage(totalNetSales, totalOrders);

        List<SalesReportResponse.SeriesItem> series = switch (periodType) {
            case DAILY -> mapDailySeries(dailyRecords);
            case WEEKLY -> mapWeeklySeries(salesDailyFetchService.findWeeklyAggregates(store.getId(), from, to));
            case MONTHLY -> mapMonthlySeries(salesDailyFetchService.findMonthlyAggregates(store.getId(), from, to));
        };

        return SalesReportResponse.builder()
                .context(SalesReportResponse.Context.builder()
                        .periodType(periodType)
                        .storeId(storeId)
                        .from(from)
                        .to(to)
                        .build())
                .summary(SalesReportResponse.Summary.builder()
                        .netSales(totalNetSales)
                        .orderCount(totalOrders)
                        .averageOrderValue(totalAvgOrderValue)
                        .canceledAmount(totalCanceledAmount)
                        .build())
                .series(series)
                .build();
    }

    private void validateInputs(Long storeId, LocalDate from, LocalDate to, PeriodType periodType) {
        if (storeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "storeId가 필요합니다.");
        }
        if (from == null || to == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "날짜 형식이 올바르지 않습니다.");
        }
        if (from.isAfter(to)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "시작일은 종료일 이후일 수 없습니다.");
        }
        if (periodType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "periodType 이 필요합니다.");
        }
    }

    private List<SalesReportResponse.SeriesItem> mapDailySeries(List<SalesDaily> records) {
        return records.stream()
                .map(record -> SalesReportResponse.SeriesItem.builder()
                        .date(record.getDate())
                        .month(YearMonth.from(record.getDate()))
                        .monthValue(record.getDate().getMonthValue())
                        .week(null)
                        .netSales(record.getDailyNetSales())
                        .orderCount(record.getOrderCount())
                        .canceledAmount(record.getDailyCanceledAmount())
                        .averageOrderValue(calculateAverage(record.getDailyNetSales(), record.getOrderCount()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<SalesReportResponse.SeriesItem> mapWeeklySeries(List<SalesDailyFetchService.SalesAggregationResult> aggregates) {
        return aggregates.stream()
                .map(aggregate -> {
                    LocalDate weekStart = aggregate.periodStart()
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    return SalesReportResponse.SeriesItem.builder()
                        .date(weekStart)
                        .month(YearMonth.from(weekStart))
                        .monthValue(weekStart.getMonthValue())
                        .week(aggregate.isoWeek())
                        .netSales(aggregate.netSales())
                        .orderCount(aggregate.orderCount())
                        .canceledAmount(aggregate.canceledAmount())
                        .averageOrderValue(calculateAverage(aggregate.netSales(), aggregate.orderCount()))
                        .build();
                })
                .collect(Collectors.toList());
    }

    private List<SalesReportResponse.SeriesItem> mapMonthlySeries(List<SalesDailyFetchService.SalesAggregationResult> aggregates) {
        return aggregates.stream()
                .map(aggregate -> {
                    YearMonth month = YearMonth.from(aggregate.periodStart());
                    return SalesReportResponse.SeriesItem.builder()
                            .date(month.atDay(1))
                            .month(month)
                            .monthValue(month.getMonthValue())
                            .week(null)
                            .netSales(aggregate.netSales())
                            .orderCount(aggregate.orderCount())
                            .canceledAmount(aggregate.canceledAmount())
                            .averageOrderValue(calculateAverage(aggregate.netSales(), aggregate.orderCount()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateAverage(long netSales, int orders) {
        if (orders <= 0) {
            return ZERO;
        }
        return BigDecimal.valueOf(netSales)
                .divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
    }
}
