package com.zyna.dev.ecommerce.inventory.service.interfaces;

import com.zyna.dev.ecommerce.inventory.dto.request.AdjustStockRequest;
import com.zyna.dev.ecommerce.inventory.dto.response.InventoryAuditResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface InventoryService {
    InventoryAuditResponse adjustStock(Long productId, AdjustStockRequest request);

    Page<InventoryAuditResponse> getInventoryLogs(
            Long productId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    );

    byte[] exportAuditLogsToExcel(Long productId, LocalDate fromDate, LocalDate toDate);

    byte[] exportAuditLogsToPdf(Long productId, LocalDate fromDate, LocalDate toDate);
}
