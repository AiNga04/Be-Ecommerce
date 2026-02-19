package com.zyna.dev.ecommerce.orders.repository;

import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.PaymentStatus;
import com.zyna.dev.ecommerce.common.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN Shipment s ON s.order = o " +
           "WHERE o.user = :user " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) " +
           "AND (:shipmentStatus IS NULL OR s.status = :shipmentStatus) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findMyOrders(@Param("user") User user, 
                             @Param("status") OrderStatus status, 
                             @Param("paymentStatus") PaymentStatus paymentStatus, 
                             @Param("shipmentStatus") ShipmentStatus shipmentStatus,
                             Pageable pageable);

    java.util.Optional<Order> findByCode(String code);

    long countByStatus(OrderStatus status);

    @Query("SELECT new com.zyna.dev.ecommerce.dashboard.dto.RevenueStatResponse(" +
           "CAST(function('to_char', o.createdAt, 'YYYY-MM-DD') AS string), SUM(o.totalPrice)) " +
           "FROM Order o " +
           "WHERE o.status IN :statuses " +
           "AND o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY function('to_char', o.createdAt, 'YYYY-MM-DD') " +
           "ORDER BY function('to_char', o.createdAt, 'YYYY-MM-DD') ASC")
    java.util.List<com.zyna.dev.ecommerce.dashboard.dto.RevenueStatResponse> getRevenueStats(
            @Param("statuses") java.util.List<OrderStatus> statuses,
            @Param("startDate") java.time.LocalDateTime startDate, 
            @Param("endDate") java.time.LocalDateTime endDate);
}
