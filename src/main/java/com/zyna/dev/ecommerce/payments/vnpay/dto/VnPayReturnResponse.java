package com.zyna.dev.ecommerce.payments.vnpay.dto;

import com.zyna.dev.ecommerce.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VnPayReturnResponse {

    private String orderCode;
    private PaymentStatus paymentStatus;

    // từ VNPay:
    private String vnpResponseCode;
    private String vnpTransactionStatus;
    private String vnpTransactionNo;
    private String vnpBankCode;
    private String vnpAmount;
    private String vnpPayDate;

    private String message;
}
