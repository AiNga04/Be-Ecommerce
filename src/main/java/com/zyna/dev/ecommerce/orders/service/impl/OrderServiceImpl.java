package com.zyna.dev.ecommerce.orders.service.impl;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.PaymentStatus;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.orders.OrderMapper;
import com.zyna.dev.ecommerce.orders.dto.request.CheckoutItemRequest;
import com.zyna.dev.ecommerce.orders.dto.request.CheckoutRequest;
import com.zyna.dev.ecommerce.orders.dto.response.OrderItemResponse;
import com.zyna.dev.ecommerce.orders.dto.response.OrderResponse;
import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.orders.models.OrderItem;
import com.zyna.dev.ecommerce.orders.repository.OrderRepository;
import com.zyna.dev.ecommerce.orders.service.interfaces.OrderService;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.repository.ProductRepository;
import com.zyna.dev.ecommerce.users.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private  final OrderMapper  orderMapper;

    @Override
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        // 1. Chuẩn bị danh sách item, check tồn kho, tính tổng tiền
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CheckoutItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ApplicationException(
                            HttpStatus.NOT_FOUND,
                            "Product not found: " + itemReq.getProductId()
                    ));

            int requestedQty = itemReq.getQuantity();
            if (requestedQty <= 0) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
            }

            // kiểm tra stock
            if (product.getStock() == null || product.getStock() < requestedQty) {
                throw new ApplicationException(
                        HttpStatus.BAD_REQUEST,
                        "Not enough stock for product: " + product.getName()
                );
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(requestedQty));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(requestedQty)
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
            total = total.add(subtotal);
        }

        // 2. Tạo Order
        Order order = Order.builder()
                .user(user)
                .totalPrice(total)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .shippingName(request.getShippingName())
                .shippingPhone(request.getShippingPhone())
                .shippingAddress(request.getShippingAddress())
                .build();

        // set quan hệ 2 chiều
        for (OrderItem oi : orderItems) {
            oi.setOrder(order);
        }
        order.setItems(orderItems);

        // 3. Giảm stock (vì checkout thành công)
        for (OrderItem oi : orderItems) {
            Product p = oi.getProduct();
            int newStock = p.getStock() - oi.getQuantity();
            p.setStock(newStock);
            // optional: lưu audit stock tự động nếu muốn
        }

        // 4. Lưu order + items + cập nhật product stock trong cùng transaction
        order = orderRepository.save(order);

        // 5. Map sang response
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForUser(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "You do not own this order");
        }

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return orders.map(this::toOrderResponse);
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(oi -> OrderItemResponse.builder()
                        .productId(oi.getProduct().getId())
                        .productName(oi.getProduct().getName())
                        .quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice())
                        .subtotal(oi.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
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
                .items(itemResponses)
                .build();
    }
}
