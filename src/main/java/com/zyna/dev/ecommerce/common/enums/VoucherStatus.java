package com.zyna.dev.ecommerce.common.enums;

public enum VoucherStatus {
    DRAFT,      // Admin tạo xong, chưa public cho user dùng
    ACTIVE,     // Đang hoạt động, user có thể apply
    INACTIVE,   // Admin tắt voucher (pause chiến dịch)
    EXPIRED     // Hết hạn (tự động hoặc do logic)
}
