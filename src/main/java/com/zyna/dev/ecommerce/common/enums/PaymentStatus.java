package com.zyna.dev.ecommerce.common.enums;

public enum PaymentStatus {
    UNPAID,     // chưa thanh toán
    PENDING,    // đã tạo yêu cầu sang VNPAY, chờ kết quả
    PAID,       // thanh toán thành công
    FAILED,     // thanh toán thất bại
    REFUNDED    // đã hoàn tiền
}
