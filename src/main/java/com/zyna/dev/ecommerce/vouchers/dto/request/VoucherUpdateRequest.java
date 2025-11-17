package com.zyna.dev.ecommerce.vouchers.dto.request;

import com.zyna.dev.ecommerce.common.enums.VoucherScope;
import com.zyna.dev.ecommerce.common.enums.VoucherType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class VoucherUpdateRequest {

    private String name;
    private String description;
    private VoucherType type;
    private VoucherScope scope;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private Integer maxUsagePerUser;
    private Integer maxUsage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
}
