package com.servease.demo.repository;

import com.servease.demo.model.entity.CashPaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashPaymentRefundRepository extends JpaRepository<CashPaymentRefund, Long> {

    boolean existsByCashPaymentId(Long cashPaymentId);
}
