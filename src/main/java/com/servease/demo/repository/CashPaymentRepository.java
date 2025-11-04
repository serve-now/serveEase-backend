package com.servease.demo.repository;

import com.servease.demo.model.entity.CashPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashPaymentRepository extends JpaRepository<CashPayment, Long> {
}
