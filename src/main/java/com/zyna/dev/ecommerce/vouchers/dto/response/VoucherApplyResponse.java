package com.zyna.dev.ecommerce.vouchers.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class VoucherApplyResponse {

    private boolean valid;
    private String message;
    private BigDecimal cartTotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal shippingDiscount;
    private BigDecimal finalPayable;
}
