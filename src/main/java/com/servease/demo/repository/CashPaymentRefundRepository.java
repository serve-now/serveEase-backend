package com.servease.demo.repository;

import com.servease.demo.model.entity.CashPaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CashPaymentRefundRepository extends JpaRepository<CashPaymentRefund, Long> {

    boolean existsByCashPaymentId(Long cashPaymentId);

    List<CashPaymentRefund> findByCashPaymentIdIn(Collection<Long> cashPaymentIds);
}
