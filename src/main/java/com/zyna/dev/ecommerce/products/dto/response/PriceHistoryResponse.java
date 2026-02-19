package com.zyna.dev.ecommerce.products.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryResponse {
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private LocalDateTime changedAt;
    private String changedBy;
    private Double percentChange;
    private String changeType; // INCREASE, DECREASE, NONE

    // Product info for global list
    private Long productId;
    private String productName;
    private String productImage;
}
