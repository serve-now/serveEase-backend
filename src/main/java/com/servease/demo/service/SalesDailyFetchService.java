package com.servease.demo.service;

import com.servease.demo.model.entity.SalesDaily;
import com.servease.demo.repository.SalesDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesDailyFetchService {

    private final SalesDailyRepository salesDailyRepository;

    public List<SalesDaily> findByStoreAndDateRange(Long storeId, LocalDate from, LocalDate to) {
        return salesDailyRepository.findByStoreIdAndDateBetweenOrderByDateAsc(storeId, from, to);
    }
}
