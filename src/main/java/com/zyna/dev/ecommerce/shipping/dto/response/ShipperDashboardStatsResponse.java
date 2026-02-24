package com.zyna.dev.ecommerce.shipping.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class ShipperDashboardStatsResponse {
    // Hôm nay
    private long pendingPickups;
    private long inProgress;
    private long deliveredToday;
    private long failedToday;
    private BigDecimal codCollectedToday;

    // Tổng quan
    private long totalDelivered;
    private long totalFailed;
    private long totalReturned;

    // Cột mốc 7 ngày qua
    private List<ShipperChartData> chartData;
}
