package com.zyna.dev.ecommerce.vouchers.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class VoucherApplyRequest {

    @NotBlank
    private String code;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal cartTotal;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal shippingFee;

    // tương lai: gửi thêm danh sách productId / category để check scope
}
