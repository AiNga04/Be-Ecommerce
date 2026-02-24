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

    Page<Shipment> findByStatus(com.zyna.dev.ecommerce.common.enums.ShipmentStatus status, Pageable pageable);

    Optional<Shipment> findByOrderId(Long orderId);
}
