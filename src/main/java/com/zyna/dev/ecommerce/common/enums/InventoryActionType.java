package com.zyna.dev.ecommerce.common.enums;

public enum InventoryActionType {
    INBOUND,      // nhập kho
    OUTBOUND,     // xuất kho
    ADJUST,       // điều chỉnh tồn kho (adjustStock ban đầu)
    CORRECTION    // log điều chỉnh cho 1 log sai trước đó
}
