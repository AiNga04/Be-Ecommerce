package com.zyna.dev.ecommerce.orders;

import com.zyna.dev.ecommerce.orders.dto.response.OrderItemResponse;
import com.zyna.dev.ecommerce.orders.dto.response.OrderResponse;
import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.orders.models.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = toOrderItemResponses(order.getItems());

        return OrderResponse.builder()
                .id(order.getId())
                .code(order.getCode())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .shippingDiscount(order.getShippingDiscount())
                .voucherCode(order.getVoucherCode())
                .shippingVoucherCode(order.getShippingVoucherCode())
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingTrackingCode(order.getShippingTrackingCode())
                .shippingCarrier(order.getShippingCarrier())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .canceledAt(order.getCanceledAt())
                .items(items)
                .build();
    }

    public List<OrderItemResponse> toOrderItemResponses(List<OrderItem> items) {
        return items.stream()
                .map(this::toOrderItemResponse)
                .toList();
    }

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .size(item.getSize())
                .image(item.getProduct() != null ? item.getProduct().getImageUrl() : null)
                .build();
    }
}
