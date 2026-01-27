package com.zyna.dev.ecommerce.shipping.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.shipping.dto.request.AssignShipmentRequest;
import com.zyna.dev.ecommerce.shipping.dto.request.ShipmentFailRequest;
import com.zyna.dev.ecommerce.shipping.dto.response.ShipmentInfoResponse;
import com.zyna.dev.ecommerce.shipping.service.interfaces.ShipmentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    // ======================= ADMIN / STAFF =======================

    /**
     * Gán shipper cho đơn hàng sau khi order đã CONFIRMED
     */
    @PostMapping("/{orderId}/assign")
    @PreAuthorize("hasAuthority('ORDER_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> assignShipper(
            @PathVariable Long orderId,
            @RequestBody AssignShipmentRequest request
    ) {
        ShipmentInfoResponse res = shipmentService.assignShipper(
                orderId,
                request.getShipperId(),
                request.getCarrierCode()
        );

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Shipment assigned successfully",
                res
        );
    }

    // ========================== SHIPPER ==========================

    @PatchMapping("/{shipmentId}/picked-up")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> markPickedUp(@PathVariable Long shipmentId) {
        return ApiResponse.successfulResponse(
                200,
                "Package picked up!",
                shipmentService.markPickedUp(shipmentId)
        );
    }

    @PatchMapping("/{shipmentId}/out-for-delivery")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> markOutForDelivery(@PathVariable Long shipmentId) {
        return ApiResponse.successfulResponse(
                200,
                "Out for delivery!",
                shipmentService.markOutForDelivery(shipmentId)
        );
    }

    @PatchMapping("/{shipmentId}/delivered")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> markDelivered(@PathVariable Long shipmentId) {
        return ApiResponse.successfulResponse(
                200,
                "Delivered successfully!",
                shipmentService.markDelivered(shipmentId)
        );
    }

    @PatchMapping("/{shipmentId}/failed")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> markFailed(
            @PathVariable Long shipmentId,
            @RequestBody ShipmentFailRequest request
    ) {
        return ApiResponse.successfulResponse(
                200,
                "Delivery failed!",
                shipmentService.markFailed(shipmentId, request.getReason())
        );
    }

    @PatchMapping("/{shipmentId}/returned")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> markReturned(
            @PathVariable Long shipmentId,
            @RequestBody ShipmentFailRequest request
    ) {
        return ApiResponse.successfulResponse(
                200,
                "Order returned!",
                shipmentService.markReturned(shipmentId, request.getReason())
        );
    }

    // USER yêu cầu trả hàng
    @PostMapping({"/{orderId}/request-return", "/{orderId}/request-return/"})
    @PreAuthorize("hasAuthority('ORDER_READ')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> requestReturn(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestBody(required = false) String reason
    ) {
        return ApiResponse.successfulResponse(
                200,
                "Return requested",
                shipmentService.userRequestReturn(orderId, clean(reason))
        );
    }

    // ADMIN / STAFF duyệt trả hàng
    @PatchMapping({"/{shipmentId}/approve-return", "/{shipmentId}/approve-return/"})
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> approveReturn(
            @PathVariable Long shipmentId,
            @RequestBody(required = false) String reason
    ) {
        return ApiResponse.successfulResponse(
                200,
                "Return approved",
                shipmentService.approveReturn(shipmentId, clean(reason))
        );
    }

    // SHIPPER xem list shipment của mình
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ShipmentInfoResponse>> getMyShipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ShipmentInfoResponse> result = shipmentService.getMyShipments(page, size);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Get my shipments successfully",
                result
        );
    }

    private String clean(String input) {
        if (input == null || input.isBlank()) return null;
        String trimmed = input.trim();
        // Nếu gửi JSON { "reason": "..." }, trích field reason
        if (trimmed.startsWith("{")) {
            try {
                JsonNode node = objectMapper.readTree(trimmed);
                JsonNode reasonNode = node.get("reason");
                if (reasonNode != null && !reasonNode.isNull()) {
                    String val = reasonNode.asText();
                    if (val != null && !val.isBlank()) return val.trim();
                }
            } catch (Exception ignored) {
            }
        }
        // Nếu gửi dạng "text" (có dấu ") thì bỏ quote ngoài
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed.isBlank() ? null : trimmed;
    }
}
