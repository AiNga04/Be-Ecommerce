package com.zyna.dev.ecommerce.shipping.dto.response;

import com.zyna.dev.ecommerce.common.enums.ShipmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentInfoResponse {

    private Long shipmentId;

    private Long orderId;
    private String orderCode;

    private ShipmentStatus status;
    private String carrier;
    private String trackingCode;

    // ---- Shipper Info ----
    private Long shipperId;
    private String shipperName;
    private String shipperPhone;

    // ---- Order Shipping Address ----
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;

    private String note;
    private Integer attempts;
    private Boolean returnRequested;
    private String returnRequestReason;
    private String returnRequestStatus;

    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime failedAt;
    private LocalDateTime returnedAt;
}
