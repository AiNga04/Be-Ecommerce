package com.zyna.dev.ecommerce.orders.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.PaymentMethod;
import com.zyna.dev.ecommerce.common.enums.PaymentStatus;

@Getter
@Setter
@Builder
public class OrderResponse {
    private Long id;
    private String code;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal shippingDiscount;
    private String voucherCode;
    private String shippingVoucherCode;

    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingTrackingCode;
    private String shippingCarrier;

    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime canceledAt;

    private List<OrderItemResponse> items;
}
