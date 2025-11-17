package com.zyna.dev.ecommerce.vouchers.dto.response;

import com.zyna.dev.ecommerce.common.enums.VoucherScope;
import com.zyna.dev.ecommerce.common.enums.VoucherType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class VoucherResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private VoucherType type;
    private VoucherScope scope;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private Integer maxUsagePerUser;
    private Integer maxUsage;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
