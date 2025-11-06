package com.servease.demo.repository;

import com.servease.demo.model.entity.PaymentCancellation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCancellationRepository extends JpaRepository<PaymentCancellation, Long> {

    boolean existsByPaymentId(Long paymentId);
}
