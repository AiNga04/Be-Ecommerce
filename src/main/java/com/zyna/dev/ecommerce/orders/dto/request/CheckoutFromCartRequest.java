package com.zyna.dev.ecommerce.orders.dto.request;

import com.zyna.dev.ecommerce.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CheckoutFromCartRequest {

    private Long shippingAddressId;

    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;

    @NotNull
    private PaymentMethod paymentMethod;

    private List<Long> cartItemIds;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal shippingFee;
    private String voucherCode;
}
