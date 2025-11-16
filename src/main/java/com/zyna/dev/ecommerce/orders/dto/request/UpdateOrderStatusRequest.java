package com.zyna.dev.ecommerce.orders.dto.request;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {

    @NotNull
    private OrderStatus status;
}
