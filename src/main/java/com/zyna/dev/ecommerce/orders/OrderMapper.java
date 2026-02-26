package com.zyna.dev.ecommerce.orders;

import com.zyna.dev.ecommerce.orders.dto.response.OrderItemResponse;
import com.zyna.dev.ecommerce.orders.dto.response.OrderResponse;
import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.orders.models.OrderItem;
import com.zyna.dev.ecommerce.shipping.models.Shipment;
import com.zyna.dev.ecommerce.shipping.repository.ShipmentRepository;
import com.zyna.dev.ecommerce.reviews.repository.ReviewRepository;
import com.zyna.dev.ecommerce.users.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ShipmentRepository shipmentRepository;
    private final ReviewRepository reviewRepository;

    public OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = toOrderItemResponses(order);

        Shipment shipment = shipmentRepository.findByOrder(order).orElse(null);
        User shipper = shipment != null ? shipment.getShipper() : null;

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
                // Shipment info
                .shipmentId(shipment != null ? shipment.getId() : null)
                .shipmentStatus(shipment != null ? shipment.getStatus() : null)
                .shipperName(shipper != null ? shipper.getFirstName() + " " + shipper.getLastName() : null)
                .shipperPhone(shipper != null ? shipper.getPhone() : null)
                .returnRequested(shipment != null ? shipment.isReturnRequested() : null)
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .canceledAt(order.getCanceledAt())
                .items(items)
                .build();
    }

    public List<OrderItemResponse> toOrderItemResponses(Order order) {
        return order.getItems().stream()
                .map(item -> toOrderItemResponse(item, order))
                .toList();
    }

    public OrderItemResponse toOrderItemResponse(OrderItem item, Order order) {
        boolean isReviewed = reviewRepository.existsByUserAndProductAndOrder(
                order.getUser(), item.getProduct(), order
        );

        return OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .size(item.getSize())
                .image(item.getProduct() != null ? item.getProduct().getImageUrl() : null)
                .isReviewed(isReviewed)
                .build();
    }
}

