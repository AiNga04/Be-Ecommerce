package com.zyna.dev.ecommerce.orders.service.interfaces;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.orders.dto.request.CheckoutFromCartRequest;
import com.zyna.dev.ecommerce.orders.dto.request.CheckoutRequest;
import com.zyna.dev.ecommerce.orders.dto.response.OrderResponse;
import org.springframework.data.domain.Page;

public interface OrderService {

    OrderResponse checkout(Long userId, CheckoutRequest request);

    OrderResponse checkoutFromCart(Long userId, CheckoutFromCartRequest request);

    OrderResponse getOrderByIdForUser(Long userId, Long orderId);

    Page<OrderResponse> getMyOrders(Long userId, int page, int size);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);

    Page<OrderResponse> getAllOrders(int page, int size);

    OrderResponse getOrderByIdForAdmin(Long orderId);
}
