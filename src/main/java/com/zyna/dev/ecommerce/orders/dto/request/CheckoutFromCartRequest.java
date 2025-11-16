package com.zyna.dev.ecommerce.orders.dto.request;

import com.zyna.dev.ecommerce.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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
}
