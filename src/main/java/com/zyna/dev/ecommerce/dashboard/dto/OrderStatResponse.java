package com.zyna.dev.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatResponse {
    private long totalOrders;
    private java.util.Map<String, Long> byStatus;
}
