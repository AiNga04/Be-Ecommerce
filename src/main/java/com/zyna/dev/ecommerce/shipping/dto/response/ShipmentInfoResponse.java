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

    private String note;
    private Integer attempts;
    private Boolean returnRequested;

    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime failedAt;
    private LocalDateTime returnedAt;
}
