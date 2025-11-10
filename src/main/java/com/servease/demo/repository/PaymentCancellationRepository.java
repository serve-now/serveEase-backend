package com.servease.demo.repository;

import com.servease.demo.model.entity.PaymentCancellation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentCancellationRepository extends JpaRepository<PaymentCancellation, Long> {

    boolean existsByPaymentId(Long paymentId);

    List<PaymentCancellation> findByPaymentIdIn(Collection<Long> paymentIds);
}
