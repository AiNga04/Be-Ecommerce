package com.zyna.dev.ecommerce.payments.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.payments.dto.PaymentLogResponse;
import com.zyna.dev.ecommerce.payments.models.PaymentTransactionLog;
import com.zyna.dev.ecommerce.payments.repository.PaymentTransactionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments/logs")
@RequiredArgsConstructor
public class PaymentTransactionLogController {

    private final PaymentTransactionLogRepository logRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_MANAGE')")
    public ApiResponse<Page<PaymentLogResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PaymentTransactionLog> logs = logRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        Page<PaymentLogResponse> response = logs.map(this::toResponse);

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Fetched payment logs successfully!",
                response
        );
    }

    private PaymentLogResponse toResponse(PaymentTransactionLog log) {
        return PaymentLogResponse.builder()
                .id(log.getId())
                .orderId(log.getOrder() != null ? log.getOrder().getId() : null)
                .orderCode(log.getOrderCode())
                .provider(log.getProvider())
                .status(log.getStatus())
                .amount(log.getAmount())
                .currency(log.getCurrency())
                .transactionId(log.getTransactionId())
                .responseCode(log.getResponseCode())
                .note(log.getNote())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
