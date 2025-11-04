package com.servease.demo.repository;

import com.servease.demo.model.entity.CashPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CashPaymentRepository extends JpaRepository<CashPayment, Long>, JpaSpecificationExecutor<CashPayment> {
}
