package com.zyna.dev.ecommerce.inventory;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.inventory.dto.request.AdjustStockRequest;
import com.zyna.dev.ecommerce.inventory.dto.response.InventoryAuditResponse;
import com.zyna.dev.ecommerce.inventory.service.interfaces.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PutMapping("/products/{productId}/stock")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ApiResponse<InventoryAuditResponse> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody AdjustStockRequest request
    ) {
        InventoryAuditResponse result = inventoryService.adjustStock(productId, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Stock adjusted successfully!",
                result
        );
    }

    @GetMapping("/audit-logs")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ApiResponse<Page<InventoryAuditResponse>> getInventoryLogs(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<InventoryAuditResponse> result =
                inventoryService.getInventoryLogs(productId, fromDate, toDate, page, size);

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get inventory audit logs successfully!",
                result
        );
    }

    @GetMapping("/audit-logs/export/excel")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<byte[]> exportAuditLogsExcel(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        byte[] data = inventoryService.exportAuditLogsToExcel(productId, fromDate, toDate);

        String filename = "inventory_audit_" + LocalDate.now() + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/audit-logs/export/pdf")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<byte[]> exportAuditLogsPdf(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        byte[] data = inventoryService.exportAuditLogsToPdf(productId, fromDate, toDate);

        String filename = "inventory_audit_" + LocalDate.now() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
