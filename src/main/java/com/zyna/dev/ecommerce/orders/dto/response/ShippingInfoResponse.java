package com.zyna.dev.ecommerce.shipping.dto.response;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShippingInfoResponse {

    private Long orderId;
    private String orderCode;
    private OrderStatus orderStatus;

    private String shippingCarrier;
    private String shippingTrackingCode;
    private Integer shippingAttempts;
    private String shippingNote;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
