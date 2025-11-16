package com.zyna.dev.ecommerce.orders.dto.request;

import com.zyna.dev.ecommerce.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckoutRequest {

    @NotEmpty
    @Valid
    private List<CheckoutItemRequest> items;

    @NotBlank
    private String shippingName;

    @NotBlank
    private String shippingPhone;

    @NotBlank
    private String shippingAddress;

    @NotNull
    private PaymentMethod paymentMethod;
}
