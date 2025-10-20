package com.servease.demo.repository;

import com.servease.demo.model.entity.SalesDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesDailyRepository extends JpaRepository<SalesDaily, Long> {
    Optional<SalesDaily> findByStoreIdAndDate(Long storeId, LocalDate date);

    List<SalesDaily> findByStoreIdAndDateBetweenOrderByDateAsc(Long storeId, LocalDate start, LocalDate end);
}
