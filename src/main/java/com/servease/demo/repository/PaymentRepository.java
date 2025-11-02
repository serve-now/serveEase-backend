package com.servease.demo.repository;

import com.servease.demo.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    boolean existsByPaymentKey(String paymentKey);

    boolean existsByExternalOrderId(String externalOrderId);

    Page<Payment> findAll(Pageable pageable);

    Optional<Payment> findWithOrderById(Long id);
}
