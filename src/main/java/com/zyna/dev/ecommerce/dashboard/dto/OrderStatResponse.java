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
    private long pending;
    private long confirmed;
    private long shipping;
    private long delivered;
    private long canceled;
    // You can add more detailed breakdown if needed
}
