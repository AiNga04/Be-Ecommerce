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
                "Đã gán người giao hàng thành công",
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
                "Đã lấy hàng!",
                shipmentService.markPickedUp(shipmentId)
        );
    }

    @PatchMapping("/{shipmentId}/out-for-delivery")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> markOutForDelivery(@PathVariable Long shipmentId) {
        return ApiResponse.successfulResponse(
                200,
                "Đang giao hàng!",
                shipmentService.markOutForDelivery(shipmentId)
        );
    }

    @PatchMapping("/{shipmentId}/delivered")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> markDelivered(@PathVariable Long shipmentId) {
        return ApiResponse.successfulResponse(
                200,
                "Giao hàng thành công!",
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
                "Giao hàng thất bại!",
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
                "Đơn hàng đã được hoàn trả!",
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
                "Đã yêu cầu trả hàng",
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
                "Đã duyệt trả hàng",
                shipmentService.approveReturn(shipmentId, clean(reason))
        );
    }

    @PatchMapping({"/{shipmentId}/reject-return", "/{shipmentId}/reject-return/"})
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> rejectReturn(
            @PathVariable Long shipmentId,
            @RequestBody(required = false) String reason
    ) {
        return ApiResponse.successfulResponse(
                200,
                "Đã từ chối trả hàng",
                shipmentService.rejectReturn(shipmentId, clean(reason))
        );
    }

    // ======================== ADMIN / STAFF ========================

    /**
     * Danh sách tất cả shipment (phân trang, lọc theo status)
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ORDER_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ShipmentInfoResponse>> getAllShipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) com.zyna.dev.ecommerce.common.enums.ShipmentStatus status,
            @RequestParam(required = false) Long shipperId,
            @RequestParam(required = false) Boolean returnRequested
    ) {
        Page<ShipmentInfoResponse> result = shipmentService.getAllShipments(page, size, status, shipperId, returnRequested);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Lấy danh mục đơn giao hàng thành công",
                result
        );
    }

    /**
     * Chi tiết 1 shipment theo ID
     */
    @GetMapping("/admin/{shipmentId}")
    @PreAuthorize("hasAuthority('ORDER_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> getShipmentById(@PathVariable Long shipmentId) {
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Lấy thông tin đơn giao hàng thành công",
                shipmentService.getShipmentById(shipmentId)
        );
    }

    /**
     * Shipment theo orderId
     */
    @GetMapping("/admin/order/{orderId}")
    @PreAuthorize("hasAuthority('ORDER_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> getShipmentByOrderId(@PathVariable Long orderId) {
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Lấy thông tin giao hàng theo đơn hàng thành công",
                shipmentService.getShipmentByOrderId(orderId)
        );
    }

    // SHIPPER xem list shipment của mình (mặc định chưa hoàn tất, hoặc theo status)
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ShipmentInfoResponse>> getMyShipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) com.zyna.dev.ecommerce.common.enums.ShipmentStatus status
    ) {
        Page<ShipmentInfoResponse> result = shipmentService.getMyShipments(page, size, status);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Lấy danh sách đơn giao hàng của tôi thành công",
                result
        );
    }

    // SHIPPER xem Dashboard Stats
    @GetMapping("/my/stats")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<com.zyna.dev.ecommerce.shipping.dto.response.ShipperDashboardStatsResponse> getMyDashboardStats(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Lấy thống kê shipper thành công",
                shipmentService.getMyDashboardStats(from, to)
        );
    }

    // SHIPPER xem Lịch sử giao hàng
    @GetMapping("/my/history")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ShipmentInfoResponse>> getMyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) com.zyna.dev.ecommerce.common.enums.ShipmentStatus status
    ) {
        Page<ShipmentInfoResponse> result = shipmentService.getMyHistory(page, size, status);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Lấy lịch sử giao hàng thành công",
                result
        );
    }

    // SHIPPER xem chi tiết 1 shipment
    @GetMapping("/my/{shipmentId}")
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShipmentInfoResponse> getMyShipmentById(@PathVariable Long shipmentId) {
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Lấy thông tin đơn giao hàng thành công",
                shipmentService.getMyShipmentById(shipmentId)
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
