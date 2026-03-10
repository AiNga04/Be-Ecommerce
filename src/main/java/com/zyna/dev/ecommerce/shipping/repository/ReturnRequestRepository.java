package com.zyna.dev.ecommerce.shipping.repository;

import com.zyna.dev.ecommerce.common.enums.ReturnRequestStatus;
import com.zyna.dev.ecommerce.shipping.models.ReturnRequest;
import com.zyna.dev.ecommerce.shipping.models.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    Optional<ReturnRequest> findFirstByShipmentAndStatusOrderByCreatedAtDesc(Shipment shipment, ReturnRequestStatus status);
}
