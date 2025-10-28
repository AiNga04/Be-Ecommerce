package com.zyna.dev.ecommerce.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    PENDING("Chờ xác minh"),
    ACTIVE("Đang hoạt động"),
    DISABLED("Bị khóa"),
    DELETED("Đã xóa");

    private final String displayName;
}
