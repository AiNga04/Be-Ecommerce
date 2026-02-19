package com.zyna.dev.ecommerce.dashboard.service.impl;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.dashboard.dto.*;
import com.zyna.dev.ecommerce.dashboard.service.DashboardService;

import java.math.BigDecimal;
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
    public RevenueDashboardResponse getRevenueStats(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        
        List<OrderStatus> statuses = Arrays.asList(
                OrderStatus.CONFIRMED, OrderStatus.SHIPPING, OrderStatus.DELIVERED
        );
        
        List<RevenueStatResponse> stats = orderRepository.getRevenueStats(statuses, start, end);
        
            BigDecimal totalRevenue = stats.stream()
                .map(RevenueStatResponse::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Growth Rate Calculation
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        LocalDateTime prevStart = start.minusDays(daysDiff);
        LocalDateTime prevEnd = start.minusNanos(1); // End right before current start

        List<RevenueStatResponse> prevStats = orderRepository.getRevenueStats(statuses, prevStart, prevEnd);
        BigDecimal prevRevenue = prevStats.stream()
                .map(RevenueStatResponse::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Double growthRate = 0.0;
        if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = totalRevenue.subtract(prevRevenue);
             growthRate = diff.divide(prevRevenue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        } else if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = 100.0;
        }

        return RevenueDashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .growthRate(growthRate)
                .dailyStats(stats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatResponse getOrderStats() {
        List<Object[]> rawStats = orderRepository.countOrdersByStatus();
        java.util.Map<String, Long> byStatus = new java.util.HashMap<>();
        long total = 0;

        for (Object[] row : rawStats) {
            OrderStatus status = (OrderStatus) row[0];
            Long count = (Long) row[1];
            byStatus.put(status.name(), count);
            total += count;
        }
        
        // Ensure all statuses are present
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.putIfAbsent(status.name(), 0L);
        }

        return OrderStatResponse.builder()
                .totalOrders(total)
                .byStatus(byStatus)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductResponse> getTopSellingProducts(int limit) {
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
        long disabled = userRepository.countByStatus(Status.DISABLED); 
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long newToday = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        return UserStatResponse.builder()
                .totalUsers(total)
                .activeUsers(active)
                .pendingUsers(pending)
                .disabledUsers(disabled)
                .newUsersToday(newToday)
                .build();
    }
}
