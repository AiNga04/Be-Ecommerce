package com.zyna.dev.ecommerce.shipping.service.interfaces;

import com.zyna.dev.ecommerce.shipping.dto.response.ShipmentInfoResponse;

public interface ShipmentService {

    ShipmentInfoResponse assignShipper(Long orderId, Long shipperId, String carrierCode);

    ShipmentInfoResponse markPickedUp(Long shipmentId);
    ShipmentInfoResponse markOutForDelivery(Long shipmentId);
    ShipmentInfoResponse markDelivered(Long shipmentId);
    ShipmentInfoResponse markFailed(Long shipmentId, String reason);
    ShipmentInfoResponse markReturned(Long shipmentId, String reason);
    ShipmentInfoResponse userRequestReturn(Long orderId, String reason);
    ShipmentInfoResponse approveReturn(Long shipmentId, String reason);
    org.springframework.data.domain.Page<ShipmentInfoResponse> getMyShipments(int page, int size);
}
