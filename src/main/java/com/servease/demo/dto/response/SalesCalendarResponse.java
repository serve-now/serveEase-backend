package com.servease.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Getter
@Builder
public class SalesCalendarResponse {
    private final Context context;
    private final List<DailySale> dailySales;
    private final List<WeeklySummary> weeklySummaries;

    @Getter
    @Builder
    public static class Context {
        private final Long storeId;
        private final YearMonth month;
        private final LocalDate from;
        private final LocalDate to;
    }

    @Getter
    @Builder
    public static class DailySale {
        private final LocalDate date;
        private final long netSales;
    }

    @Getter
    @Builder
    public static class WeeklySummary {
        private final int weekNumber;
        private final int isoWeek;
        private final int weekYear;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final long netSales;
    }
}
