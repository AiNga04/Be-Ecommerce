package com.zyna.dev.ecommerce.inventory;

import com.zyna.dev.ecommerce.inventory.dto.response.InventoryAuditResponse;
import com.zyna.dev.ecommerce.inventory.models.InventoryAuditLog;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryAuditResponse toResponse(InventoryAuditLog log) {

        return InventoryAuditResponse.builder()
                .id(log.getId())
                .productId(log.getProduct().getId())
                .productName(log.getProduct().getName())
                .changedByUserId(log.getChangedBy().getId())
                .changedByUserName(
                        log.getChangedBy().getFirstName() + " " +
                                log.getChangedBy().getLastName()
                )
                .oldStock(log.getOldStock())
                .newStock(log.getNewStock())
                .reason(log.getReason())
                .changedAt(log.getChangedAt())
                .build();
    }
}
