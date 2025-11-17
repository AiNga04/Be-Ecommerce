package com.zyna.dev.ecommerce.vouchers.dto.request;

import com.zyna.dev.ecommerce.common.enums.VoucherScope;
import com.zyna.dev.ecommerce.common.enums.VoucherType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class VoucherCreateRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private VoucherType type;

    private VoucherScope scope = VoucherScope.GLOBAL;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minOrderValue;

    private Integer maxUsagePerUser;

    private Integer maxUsage;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
