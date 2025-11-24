package com.zyna.dev.ecommerce.payments.repository;

import com.zyna.dev.ecommerce.payments.models.PaymentTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionLogRepository extends JpaRepository<PaymentTransactionLog, Long> {
}
