package com.zyna.dev.ecommerce.shipping.repository;

import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.shipping.models.Shipment;
import com.zyna.dev.ecommerce.users.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrder(Order order);

    boolean existsByOrderId(Long orderId);

    List<Shipment> findByShipper(User shipper);

    Page<Shipment> findByShipper(User shipper, Pageable pageable);

    Page<Shipment> findByShipperAndStatusNotIn(User shipper, java.util.Collection<com.zyna.dev.ecommerce.common.enums.ShipmentStatus> statuses, Pageable pageable);

    Page<Shipment> findByShipperAndStatus(User shipper, com.zyna.dev.ecommerce.common.enums.ShipmentStatus status, Pageable pageable);

    // ADMIN
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Shipment s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:shipperId IS NULL OR s.shipper.id = :shipperId) AND " +
           "(:returnRequested IS NULL OR s.returnRequested = :returnRequested)")
    Page<Shipment> findByAdminFilters(@org.springframework.data.repository.query.Param("status") com.zyna.dev.ecommerce.common.enums.ShipmentStatus status,
                                      @org.springframework.data.repository.query.Param("shipperId") Long shipperId,
                                      @org.springframework.data.repository.query.Param("returnRequested") Boolean returnRequested,
                                      Pageable pageable);

    Optional<Shipment> findByOrderId(Long orderId);

    // SHIPPER DASHBOARD
    Page<Shipment> findByShipperAndStatusIn(User shipper, java.util.Collection<com.zyna.dev.ecommerce.common.enums.ShipmentStatus> statuses, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Shipment s WHERE s.shipper = :shipper AND s.status IN :statuses")
    long countByShipperAndStatuses(@org.springframework.data.repository.query.Param("shipper") User shipper, @org.springframework.data.repository.query.Param("statuses") java.util.List<com.zyna.dev.ecommerce.common.enums.ShipmentStatus> statuses);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Shipment s WHERE s.shipper = :shipper AND s.status = :status AND s.deliveredAt >= :startOfDay")
    long countDeliveredSince(@org.springframework.data.repository.query.Param("shipper") User shipper, @org.springframework.data.repository.query.Param("status") com.zyna.dev.ecommerce.common.enums.ShipmentStatus status, @org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Shipment s WHERE s.shipper = :shipper AND s.status = :status AND s.failedAt >= :startOfDay")
    long countFailedSince(@org.springframework.data.repository.query.Param("shipper") User shipper, @org.springframework.data.repository.query.Param("status") com.zyna.dev.ecommerce.common.enums.ShipmentStatus status, @org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.totalPrice) FROM Shipment s JOIN s.order o " +
           "WHERE s.shipper = :shipper AND s.status = 'DELIVERED' " +
           "AND o.paymentMethod = 'CASH_ON_DELIVERY' AND s.deliveredAt >= :startOfDay")
    java.math.BigDecimal sumCodCollectedSince(@org.springframework.data.repository.query.Param("shipper") User shipper, @org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay);

    // LIFETIME
    long countByShipperAndStatus(User shipper, com.zyna.dev.ecommerce.common.enums.ShipmentStatus status);

    // 7 DAYS CHART DATA
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Shipment s WHERE s.shipper = :shipper AND s.status IN :statuses AND " +
           "(s.deliveredAt >= :since OR s.failedAt >= :since OR s.returnedAt >= :since)")
    List<Shipment> findRecentHistoryByShipper(@org.springframework.data.repository.query.Param("shipper") User shipper, 
                                              @org.springframework.data.repository.query.Param("statuses") java.util.Collection<com.zyna.dev.ecommerce.common.enums.ShipmentStatus> statuses, 
                                              @org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since);
}
