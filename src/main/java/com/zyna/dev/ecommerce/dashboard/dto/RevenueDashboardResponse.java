package com.zyna.dev.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDashboardResponse {
    private BigDecimal totalRevenue;
    private Double growthRate; // Percentage change vs previous period
    private List<RevenueStatResponse> dailyStats;
}
