package com.zyna.dev.ecommerce.common.enums;

public enum ShipmentStatus {
    PENDING_ASSIGN,   // Order đã confirm nhưng chưa gán shipper
    ASSIGNED,         // Đã gán shipper
    PICKED_UP,        // Shipper đã lấy hàng
    IN_DELIVERY,      // Đang giao hàng
    DELIVERED,        // Giao thành công
    FAILED,           // Giao thất bại
    RETURNED          // Trả hàng về
}
