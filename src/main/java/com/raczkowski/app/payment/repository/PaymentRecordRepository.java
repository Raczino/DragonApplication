package com.raczkowski.app.payment.repository;

import com.raczkowski.app.payment.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findByStripeSessionId(String stripeSessionId);
}