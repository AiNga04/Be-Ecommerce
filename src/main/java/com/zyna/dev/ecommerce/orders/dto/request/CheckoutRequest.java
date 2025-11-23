package com.zyna.dev.ecommerce.orders.dto.request;

import com.zyna.dev.ecommerce.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CheckoutRequest {

    @NotEmpty
    @Valid
    private List<CheckoutItemRequest> items;

    // ✅ Nếu dùng address book thì gửi field này
    private Long shippingAddressId;

    // ✅ Nếu KHÔNG dùng addressId thì FE sẽ gửi 3 field dưới
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;

    @NotNull
    private PaymentMethod paymentMethod;

    @DecimalMin("0.0")
    private BigDecimal shippingFee; // optional: null => default 30k in service
    private String voucherCode;          // giảm giá sản phẩm/tổng đơn (percentage/fixed)
    private String shippingVoucherCode;  // giảm phí ship (freeship)
}
