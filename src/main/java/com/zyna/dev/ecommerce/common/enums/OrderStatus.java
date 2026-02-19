package com.zyna.dev.ecommerce.common.enums;

public enum OrderStatus {
    PENDING,      // mới tạo
    CONFIRMED,    // đã xác nhận xử lý
    SHIPPING,     // đang giao hàng
    DELIVERED,    // đã giao hàng thành công
    CANCELED      // đã huỷ
}
