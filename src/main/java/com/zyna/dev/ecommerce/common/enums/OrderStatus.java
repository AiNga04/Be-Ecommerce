package com.zyna.dev.ecommerce.common.enums;

public enum OrderStatus {
    PENDING,      // mới tạo
    CONFIRMED,    // đã xác nhận xử lý
    SHIPPED,      // đã giao cho đơn vị vận chuyển
    DELIVERED,    // đã giao thành công
    CANCELED      // đã huỷ
}
