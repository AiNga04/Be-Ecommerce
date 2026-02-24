package com.zyna.dev.ecommerce.shipping.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ShipperDashboardStatsResponse {
    private long pendingPickups;
    private long inProgress;
    private long deliveredToday;
    private long failedToday;
    private BigDecimal codCollectedToday;
}
