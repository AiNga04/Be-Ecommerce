package com.zyna.dev.ecommerce.dashboard.service.impl;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.dashboard.dto.*;
import com.zyna.dev.ecommerce.dashboard.service.DashboardService;
import com.zyna.dev.ecommerce.orders.repository.OrderItemRepository;
import com.zyna.dev.ecommerce.orders.repository.OrderRepository;
import com.zyna.dev.ecommerce.products.models.ProductSize;
import com.zyna.dev.ecommerce.products.repository.ProductSizeRepository;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductSizeRepository productSizeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RevenueStatResponse> getRevenueStats(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        // Count PAID orders only (can adjust to include CONFIRMED etc if needed)
        // For revenue, usually PAID or DELIVERED is best.
        // Let's use PAID status if payment status is reliable, or status = DELIVERED.
        // In this query we use status IN (...) to be safer if paymentStatus is not always set for COD.
        // Actually OrderRepository query uses status IN list.
        List<OrderStatus> statuses = Arrays.asList(
                OrderStatus.CONFIRMED, OrderStatus.SHIPPING, OrderStatus.DELIVERED
        );
        // Note: COMPLETED might not exist in enum, check first.
        
        return orderRepository.getRevenueStats(statuses, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatResponse getOrderStats() {
        return OrderStatResponse.builder()
                .pending(orderRepository.countByStatus(OrderStatus.PENDING))
                .confirmed(orderRepository.countByStatus(OrderStatus.CONFIRMED))
                .shipping(orderRepository.countByStatus(OrderStatus.SHIPPING))
                .delivered(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .canceled(orderRepository.countByStatus(OrderStatus.CANCELED))
                .totalOrders(orderRepository.count())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductResponse> getTopSellingProducts(int limit) {
        // Only count valid orders
        List<OrderStatus> validStatuses = Arrays.asList(
                OrderStatus.CONFIRMED, OrderStatus.SHIPPING, OrderStatus.DELIVERED
        );
        return orderItemRepository.findTopSellingProducts(validStatuses, PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockResponse> getLowStockProducts(int threshold) {
        List<ProductSize> lowStocks = productSizeRepository.findByQuantityLessThan(threshold);

        return lowStocks.stream()
                .map(ps -> LowStockResponse.builder()
                        .productId(ps.getProduct().getId())
                        .productName(ps.getProduct().getName())
                        .imageUrl(ps.getProduct().getImageUrl())
                        .sizeName(ps.getSize().getName())
                        .stock(ps.getQuantity())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatResponse getUserStats() {
        long total = userRepository.count();
        long active = userRepository.countByStatus(Status.ACTIVE);
        long pending = userRepository.countByStatus(Status.PENDING);
        // Locked/Disabled? Status might be LOCKED or DISABLED.
        // Using countByStatusNot(ACTIVE) - pending?
        // Or just count specific status if enum has it.
        // Enum: PENDING, ACTIVE, DISABLED, DELETED.
        long disabled = userRepository.countByStatus(Status.DISABLED); 
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long newToday = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        return UserStatResponse.builder()
                .totalUsers(total)
                .activeUsers(active)
                .pendingUsers(pending)
                .lockedUsers(disabled)
                .newUsersToday(newToday)
                .build();
    }
}
