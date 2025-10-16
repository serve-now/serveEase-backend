package com.servease.demo.repository;

import com.servease.demo.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByPaymentKey(String paymentKey);

    boolean existsByExternalOrderId(String externalOrderId);
}
