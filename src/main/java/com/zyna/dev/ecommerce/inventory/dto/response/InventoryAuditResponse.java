package com.zyna.dev.ecommerce.inventory.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryAuditResponse {

    private Long id;

    private Long productId;
    private String productName;

    private Long changedByUserId;
    private String changedByUserName;

    private int oldStock;
    private int newStock;

    private String reason;
    private LocalDateTime changedAt;
}
