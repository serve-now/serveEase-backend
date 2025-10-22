package com.servease.demo.dto.response;

import com.servease.demo.model.enums.PeriodType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Getter
@Builder
public class SalesReportResponse {
    private final Context context;
    private final Summary summary;
    private final List<SeriesItem> series;

    @Getter
    @Builder
    public static class Context {
        private final PeriodType periodType;
        private final Long storeId;
        private final LocalDate from;
        private final LocalDate to;
    }

    @Getter
    @Builder
    public static class Summary {
        private final long netSales;
        private final int orderCount;
        private final BigDecimal averageOrderValue;
        private final long canceledAmount;
    }

    @Getter
    @Builder
    public static class SeriesItem {
        private final LocalDate date;
        private final YearMonth month;
        private final Integer week;
        private final long netSales;
        private final int orderCount;
        private final BigDecimal averageOrderValue;
        private final long canceledAmount;
    }
}
