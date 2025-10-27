package com.servease.demo.service;

import com.servease.demo.dto.response.SalesCalendarResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.SalesDaily;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesCalendarService {

    private final SalesDailyFetchService salesDailyFetchService;
    private final StoreService storeService;

    public SalesCalendarResponse getSalesCalendar(Long storeId, YearMonth month) {
        validateInputs(storeId, month);

        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        storeService.getStore(storeId);

        List<SalesDaily> dailyRecords = salesDailyFetchService.findByStoreAndDateRange(storeId, from, to);
        Map<LocalDate, SalesDaily> dailyRecordMap = dailyRecords.stream()
                .collect(Collectors.toMap(
                        SalesDaily::getDate,
                        Function.identity(),
                        (existing, replacement) -> replacement));

        List<SalesCalendarResponse.DailySale> dailySales = new ArrayList<>();
        Map<WeekKey, WeeklyAccumulator> weeklyAggregates = new LinkedHashMap<>();

        WeekFields weekFields = WeekFields.ISO;

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            SalesDaily record = dailyRecordMap.get(date);
            long netSales = record != null ? record.getDailyNetSales() : 0L;
            dailySales.add(SalesCalendarResponse.DailySale.builder()
                    .date(date)
                    .netSales(netSales)
                    .build());

            int isoWeek = date.get(weekFields.weekOfWeekBasedYear());
            int weekYear = date.get(weekFields.weekBasedYear());
            LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate weekEnd = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            LocalDate boundedStart = weekStart.isBefore(from) ? from : weekStart;
            LocalDate boundedEnd = weekEnd.isAfter(to) ? to : weekEnd;

            WeekKey key = new WeekKey(weekYear, isoWeek);
            WeeklyAccumulator accumulator = weeklyAggregates.computeIfAbsent(
                    key, k -> new WeeklyAccumulator(isoWeek, weekYear, boundedStart, boundedEnd));

            accumulator.accumulate(netSales);
            accumulator.updateBounds(boundedStart, boundedEnd);
        }

        List<SalesCalendarResponse.WeeklySummary> weeklySummaries = new ArrayList<>();
        int weekNumber = 1;
        for (WeeklyAccumulator accumulator : weeklyAggregates.values()) {
            weeklySummaries.add(SalesCalendarResponse.WeeklySummary.builder()
                    .weekNumber(weekNumber++)
                    .isoWeek(accumulator.isoWeek)
                    .weekYear(accumulator.weekYear)
                    .startDate(accumulator.startDate)
                    .endDate(accumulator.endDate)
                    .netSales(accumulator.netSales)
                    .build());
        }

        return SalesCalendarResponse.builder()
                .context(SalesCalendarResponse.Context.builder()
                        .storeId(storeId)
                        .month(month)
                        .from(from)
                        .to(to)
                        .build())
                .dailySales(dailySales)
                .weeklySummaries(weeklySummaries)
                .build();
    }

    private void validateInputs(Long storeId, YearMonth month) {
        if (storeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "storeId가 필요합니다.");
        }
        if (month == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "month가 필요합니다.");
        }
    }

    private record WeekKey(int weekYear, int isoWeek) {
    }

    private static final class WeeklyAccumulator {
        private final int isoWeek;
        private final int weekYear;
        private LocalDate startDate;
        private LocalDate endDate;
        private long netSales;

        private WeeklyAccumulator(int isoWeek, int weekYear, LocalDate startDate, LocalDate endDate) {
            this.isoWeek = isoWeek;
            this.weekYear = weekYear;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        private void accumulate(long netSales) {
            this.netSales += netSales;
        }

        private void updateBounds(LocalDate startDate, LocalDate endDate) {
            if (startDate.isBefore(this.startDate)) {
                this.startDate = startDate;
            }
            if (endDate.isAfter(this.endDate)) {
                this.endDate = endDate;
            }
        }
    }
}
