package com.servease.demo.controller;

import com.servease.demo.dto.response.SalesCalendarResponse;
import com.servease.demo.dto.response.SalesReportResponse;
import com.servease.demo.model.enums.PeriodType;
import com.servease.demo.service.SalesCalendarService;
import com.servease.demo.service.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports/sales")
@RequiredArgsConstructor
public class SalesReportController {

    private final SalesReportService salesReportService;
    private final SalesCalendarService salesCalendarService;

    @GetMapping
    public SalesReportResponse getSalesReport(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAILY") PeriodType periodType
    ) {
        return salesReportService.getSalesReport(storeId, from, to, periodType);
    }

    @GetMapping("/calendar")
    public SalesCalendarResponse getSalesCalendar(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return salesCalendarService.getSalesCalendar(storeId, month);
    }
}
