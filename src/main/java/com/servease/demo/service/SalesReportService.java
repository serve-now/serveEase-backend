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
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            case DAILY -> mapDailySeries(from, to, dailyRecords);
            case WEEKLY -> mapWeeklySeries(from, to, salesDailyFetchService.findWeeklyAggregates(store.getId(), from, to));
            case MONTHLY -> mapMonthlySeries(from, to, salesDailyFetchService.findMonthlyAggregates(store.getId(), from, to));
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

    private List<SalesReportResponse.SeriesItem> mapDailySeries(LocalDate from,
                                                                LocalDate to,
                                                                List<SalesDaily> records) {
        Map<LocalDate, SalesDaily> recordMap = records.stream()
                .collect(Collectors.toMap(
                        SalesDaily::getDate,
                        record -> record,
                        (existing, replacement) -> replacement));

        return from.datesUntil(to.plusDays(1))
                .map(date -> {
                    SalesDaily record = recordMap.get(date);
                    long netSales = record != null ? record.getDailyNetSales() : 0L;
                    int orderCount = record != null ? record.getOrderCount() : 0;
                    long canceledAmount = record != null ? record.getDailyCanceledAmount() : 0L;
                    return SalesReportResponse.SeriesItem.builder()
                            .date(date)
                            .month(YearMonth.from(date))
                            .week(null)
                            .netSales(netSales)
                            .orderCount(orderCount)
                            .canceledAmount(canceledAmount)
                            .averageOrderValue(calculateAverage(netSales, orderCount))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<SalesReportResponse.SeriesItem> mapWeeklySeries(LocalDate from,
                                                                 LocalDate to,
                                                                 List<SalesDailyFetchService.SalesAggregationResult> aggregates) {
        Map<LocalDate, SalesDailyFetchService.SalesAggregationResult> aggregateMap = aggregates.stream()
                .collect(Collectors.toMap(
                        aggregate -> aggregate.periodStart().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                        aggregate -> aggregate,
                        (existing, replacement) -> replacement));

        LocalDate firstWeekStart = from.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastWeekStart = to.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<SalesReportResponse.SeriesItem> series = new ArrayList<>();
        for (LocalDate weekStart = firstWeekStart; !weekStart.isAfter(lastWeekStart); weekStart = weekStart.plusWeeks(1)) {
            SalesDailyFetchService.SalesAggregationResult aggregate = aggregateMap.get(weekStart);

            long netSales = aggregate != null ? aggregate.netSales() : 0L;
            int orderCount = aggregate != null ? aggregate.orderCount() : 0;
            long canceledAmount = aggregate != null ? aggregate.canceledAmount() : 0L;
            int isoWeek = aggregate != null && aggregate.isoWeek() != null
                    ? aggregate.isoWeek()
                    : weekStart.get(WeekFields.ISO.weekOfWeekBasedYear());

            series.add(SalesReportResponse.SeriesItem.builder()
                    .date(weekStart)
                    .month(YearMonth.from(weekStart))
                    .monthValue(weekStart.getMonthValue())
                    .week(isoWeek)
                    .netSales(netSales)
                    .orderCount(orderCount)
                    .canceledAmount(canceledAmount)
                    .averageOrderValue(calculateAverage(netSales, orderCount))
                    .build());
        }

        return series;
    }

    private List<SalesReportResponse.SeriesItem> mapMonthlySeries(LocalDate from,
                                                                  LocalDate to,
                                                                  List<SalesDailyFetchService.SalesAggregationResult> aggregates) {
        Map<YearMonth, SalesDailyFetchService.SalesAggregationResult> aggregateMap = aggregates.stream()
                .collect(Collectors.toMap(
                        aggregate -> YearMonth.from(aggregate.periodStart()),
                        aggregate -> aggregate,
                        (existing, replacement) -> replacement));

        YearMonth startMonth = YearMonth.from(from);
        YearMonth endMonth = YearMonth.from(to);

        List<SalesReportResponse.SeriesItem> series = new ArrayList<>();
        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            SalesDailyFetchService.SalesAggregationResult aggregate = aggregateMap.get(month);

            long netSales = aggregate != null ? aggregate.netSales() : 0L;
            int orderCount = aggregate != null ? aggregate.orderCount() : 0;
            long canceledAmount = aggregate != null ? aggregate.canceledAmount() : 0L;

            series.add(SalesReportResponse.SeriesItem.builder()
                    .date(month.atDay(1))
                    .month(month)
                    .monthValue(month.getMonthValue())
                    .week(null)
                    .netSales(netSales)
                    .orderCount(orderCount)
                    .canceledAmount(canceledAmount)
                    .averageOrderValue(calculateAverage(netSales, orderCount))
                    .build());
        }

        return series;
    }

    private BigDecimal calculateAverage(long netSales, int orders) {
        if (orders <= 0) {
            return ZERO;
        }
        return BigDecimal.valueOf(netSales)
                .divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
    }
}
