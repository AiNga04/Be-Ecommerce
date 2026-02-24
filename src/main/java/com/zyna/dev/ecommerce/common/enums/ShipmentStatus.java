package com.zyna.dev.ecommerce.common.enums;

public enum ShipmentStatus {
    PENDING_ASSIGN,   // Order đã confirm nhưng chưa gán shipper
    ASSIGNED,         // Đã gán shipper
    PICKED_UP,        // Shipper đã lấy hàng
    IN_DELIVERY,      // Đang giao hàng
    DELIVERED,        // Giao thành công
    FAILED,           // Giao thất bại
    RETURN_APPROVED,  // Admin đã duyệt trả hàng — chờ Shipper đến lấy lại
    RETURNED          // Shipper đã lấy hàng về kho
}
