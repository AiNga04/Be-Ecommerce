package com.zyna.dev.ecommerce.shipping.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ShipperChartData {
    private String date; // YYYY-MM-DD
    private long deliveredCount;
    private long failedCount;
    private long returnedCount;
    private BigDecimal codCollected;
}
